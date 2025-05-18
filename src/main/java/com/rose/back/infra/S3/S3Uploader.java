package com.rose.back.infra.S3;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3; // S3Config

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(Path filePath) {
        String key = filePath.getFileName().toString();
        log.info("S3 업로드 시작: 버킷={}, 키={}", bucket, key);
        amazonS3.putObject(new PutObjectRequest(bucket, key, filePath.toFile()));
        return amazonS3.getUrl(bucket, key).toString();
    }
}