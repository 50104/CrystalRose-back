package com.rose.back.board.content.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3 amazonS3Client; // S3Config

    @Value("${cloud.aws.s3.bucket}")
    private String s3Bucket;

    @Value("${img.upload-dir}")
    private String imgDir;

    public String imageUpload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("파일이 없습니다.");
        }
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf("."));
        String uuidFileName = UUID.randomUUID() + ext;

        Path uploadDir = Paths.get(imgDir);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path filePath = uploadDir.resolve(uuidFileName);
        file.transferTo(filePath.toFile());
        String s3Url = uploadToS3(filePath); // S3 업로드
        Files.delete(filePath); // 로컬 파일 삭제
        return s3Url;
    }

    private String uploadToS3(Path filePath) {
        amazonS3Client.putObject(new PutObjectRequest(s3Bucket, filePath.getFileName().toString(), filePath.toFile()));
        return amazonS3Client.getUrl(s3Bucket, filePath.getFileName().toString()).toString();
    }
}