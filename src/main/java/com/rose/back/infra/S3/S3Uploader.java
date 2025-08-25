package com.rose.back.infra.S3;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("https://dodorose.com")
    private String cloudFrontDomain;

    public String uploadFile(String folderName, MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        log.info("S3Uploader 호출됨 - originalName: {}, folder: {}", originalName, folderName);

        String extension = getExtension(originalName);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        String key = String.format("uploads/%s/%s/%s%s", folderName, timeStamp, uuid, extension);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(resolveContentType(extension))
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("S3 업로드 성공: key={}", key);
            return generateCloudFrontUrl(key);
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new IOException("S3 업로드 실패", e);
        } catch (S3Exception e) {
            log.error("S3 업로드 실패 (S3 예외): {}", e.getMessage());
            throw new IOException("S3 업로드 실패", e);
        }
    }

    public String uploadProfile(MultipartFile file, String userId) throws IOException {
        String originalName = file.getOriginalFilename();
        log.info("S3 프로필 업로드 호출됨 - originalName: {}, userId: {}", originalName, userId);

        String extension = getExtension(originalName);
        String key = String.format("profiles/%s_%s%s", userId, UUID.randomUUID(), extension);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(resolveContentType(extension))
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("S3 프로필 업로드 성공: key={}", key);
            return generateCloudFrontUrl(key);
        } catch (IOException e) {
            log.error("S3 프로필 업로드 실패: {}", e.getMessage());
            throw new IOException("S3 업로드 실패", e);
        } catch (S3Exception e) {
            log.error("S3 프로필 업로드 실패 (S3 예외): {}", e.getMessage());
            throw new IOException("S3 업로드 실패", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            log.warn("파일명 없음: {}", filename);
            return ".bin";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private String resolveContentType(String extension) {
        return switch (extension) {
            case ".webp" -> "image/webp";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    private String generateCloudFrontUrl(String key) {
        String domain = cloudFrontDomain.endsWith("/") ? cloudFrontDomain.substring(0, cloudFrontDomain.length() - 1) : cloudFrontDomain;
        return domain + "/" + key;
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = fileUrl.replace(cloudFrontDomain + "/", "");
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 삭제 완료: {}", key);
        } catch (S3Exception e) {
            log.error("S3 삭제 실패 (S3 예외): {}", e.getMessage());
        } catch (Exception e) {
            log.error("S3 삭제 실패 (기타 예외): {}", e.getMessage());
        }
    }
}
