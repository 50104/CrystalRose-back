package com.rose.back.infra.S3;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, Long boardNo) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timeStamp = new SimpleDateFormat("yyyyMMdd/HH/mmss").format(new Date());
        String key = String.format("boards/%s/%s%s", timeStamp, uuid, extension);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucket, key, inputStream, metadata);
            log.info("S3 업로드 성공: key={}", key);
            return amazonS3.getUrl(bucket, key).toString(); // 전체 URL 반환
        } catch (IOException e) {
            log.error("S3 업로드 중 IO 오류: {}", e.getMessage());
            throw new IOException("S3 업로드 실패", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    public void deleteFile(String fileUrl) {
        try { 
            String key = extractKeyFromUrl(fileUrl);
            amazonS3.deleteObject(bucket, key);
            log.info("S3 이미지 삭제 완료: {}", key);
        } catch (Exception e) {
            log.error("S3 이미지 삭제 실패: {}", fileUrl, e);
        }
    }

    private String extractKeyFromUrl(String url) {
        return url.substring(url.indexOf(".com/") + 5);
    }
}
