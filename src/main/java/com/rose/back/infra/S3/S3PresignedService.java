package com.rose.back.infra.S3;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.infra.S3.ImageTempEntity.DomainType;
import com.rose.back.infra.S3.dto.PreSignedUrlResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class S3PresignedService {

    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final ImageTempRepository imageTempRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String regionValue;

    @Value("${app.cloudfront.domain:https://dodorose.com}")
    private String cloudFrontDomain;

    public S3PresignedService(S3Presigner presigner, S3Client s3Client, ImageTempRepository imageTempRepository) {
        this.presigner = presigner;
        this.s3Client = s3Client;
        this.imageTempRepository = imageTempRepository;
    }

    public PreSignedUrlResponse generatePreSignedUrl(String originalFileName, String contentType, String folderName) {
        String key = generateUniqueFileName(originalFileName, folderName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(p -> p
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(5))
        );

        log.info("Presigned 생성: key={}, url={}", key, presigned.url().toString());

        return new PreSignedUrlResponse(presigned.url().toString(), generateCloudFrontUrl(key), key);
    }

    public PreSignedUrlResponse generateProfilePreSignedUrl(String fileName, String contentType, String userId) {
        String extension = getExtension(fileName);
        String key = String.format("profiles/%s_%s%s", userId, UUID.randomUUID(), extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(p -> p
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(5))
        );

        log.info("프로필 Presigned 생성: key={}, url={}", key, presigned.url().toString());
        return new PreSignedUrlResponse(presigned.url().toString(), generateCloudFrontUrl(key), key);
    }

    public void deleteFile(String key) {
        if (key == null || key.trim().isEmpty()) {
            log.warn("deleteFile 호출: 빈 key 전달");
            throw new IllegalArgumentException("삭제할 키가 없습니다.");
        }

        if (key.contains("..")) { // 의심 패턴 차단
            log.warn("삭제 불가 - 의심스러운 키: {}", key);
            throw new IllegalArgumentException("잘못된 파일 키입니다.");
        }

        try {
            DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(delReq);

            log.info("S3 파일 삭제 완료: key={}", key);
        } catch (S3Exception e) {
            int status = e.statusCode();
            if (status == 404) {
                log.warn("삭제 대상 S3 객체 없음(key={}): statusCode=404", key);
                return;
            }
            log.error("S3Exception during deleteFile(key={}): status={}, msg={}", key, status, e.getMessage());
            throw new RuntimeException("S3 파일 삭제 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("Unexpected error during deleteFile(key={}): {}", key, e.getMessage(), e);
            throw new RuntimeException("파일 삭제 처리 중 예외가 발생했습니다.");
        }
    }

    // 기존 유틸 메소드들
    public String extractKeyFromUrl(String fileUrl) {
        String domain = cloudFrontDomain.endsWith("/") ? cloudFrontDomain.substring(0, cloudFrontDomain.length() - 1) : cloudFrontDomain;
        if (fileUrl.startsWith(domain)) {
            return fileUrl.substring(domain.length() + 1);
        }
        return fileUrl;
    }

    private String generateUniqueFileName(String originalFileName, String folderName) {
        String extension = getExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        return String.format("uploads/%s/%s/%s%s", folderName, timeStamp, uuid, extension);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            log.warn("파일명에 확장자 없음: {}", filename);
            return ".bin";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private String generateCloudFrontUrl(String key) {
        String domain = cloudFrontDomain.endsWith("/") ? cloudFrontDomain.substring(0, cloudFrontDomain.length() - 1) : cloudFrontDomain;
        return domain + "/" + key;
    }

    @PreDestroy
    public void closeResources() {
        try {
            presigner.close();
        } catch (Exception e) {
            log.warn("Presigner close 실패", e);
        }
        try {
            s3Client.close();
        } catch (Exception e) {
            log.warn("S3Client close 실패", e);
        }
    }

    public boolean isValidFileType(String contentType, DomainType domainType) { // 파일 타입 검증
        if (contentType == null) return false;

        return switch (domainType) {
            case BOARD, USER, ROSE, WIKI, DIARY -> isValidImageType(contentType);
        };
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/gif") ||
                contentType.equals("image/webp");
    }

    public String generateFolderPath(DomainType domainType, String folderName, String username) { // 폴더 경로 생성
        if (folderName != null && !folderName.trim().isEmpty()) {
            return folderName;
        }

        return switch (domainType) {
            case BOARD -> "boards";
            case USER -> "profiles/" + username;
            case ROSE -> "roses";
            case WIKI -> "wikis";
            case DIARY -> "diaries";
        };
    }

    public void saveToTempTable(String fileUrl, String key, DomainType domainType) { // 임시 테이블 저장
        ImageTempEntity tempEntity = ImageTempEntity.builder()
                .fileUrl(fileUrl)
                .s3Key(key)
                .domainType(domainType)
                .uploadedAt(new java.util.Date())
                .build();
        
        imageTempRepository.save(tempEntity);
        log.info("임시 테이블 저장 완료: fileUrl={}, s3Key={}, domainType={}", fileUrl, key, domainType);
    }

    public boolean hasDeletePermission(String key, CustomUserDetails user) { // 삭제 권한 확인
        if (user.getAuthorities().stream() // 관리자는 모든 파일 삭제 가능
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        if (key.contains("profiles/" + user.getUsername())) { // 프로필 이미지는 본인만 삭제 가능
            return true;
        }

        // 기타 권한 검증 로직 추가 가능
        return true; // 임시로 모든 사용자에게 삭제 권한 부여
    }
}
