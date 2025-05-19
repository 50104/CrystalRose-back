package com.rose.back.infra.S3;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
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

    public String uploadFile(@RequestParam("file") MultipartFile file, 
                            @RequestParam(value = "boardNo", required = false) Long boardNo) throws IOException {
        String originalName = file.getOriginalFilename();
        log.info("S3Uploader 호출됨 - originalName: {}, size: {}, boardNo: {}", originalName, file.getSize(), boardNo);
        String extension = getExtension(originalName);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
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
            log.warn("파일명 null 또는 확장자 없음: {}", filename);
            return ".bin";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = fileUrl.replace("https://cristalrose-web.s3.ap-northeast-2.amazonaws.com/", "");
            amazonS3.deleteObject(bucket, key);
            log.info("S3 삭제 완료: {}", key);
        } catch (AmazonServiceException e) {
            log.error("S3 삭제 실패 (Amazon 예외): {}", e.getErrorMessage());
        } catch (Exception e) {
            log.error("S3 삭제 실패 (기타 예외): {}", e.getMessage());
        }
    }
}
