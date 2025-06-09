package com.rose.back.domain.board.service;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ContentImageEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.ContentImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.ImageUrlExtractor;
import com.rose.back.infra.S3.S3Uploader;
import com.rose.back.infra.S3.ImageTempEntity.DomainType;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentImageService {
  
    private final S3Uploader s3Uploader;
    private final ContentImageRepository contentImageRepository;
    private final ImageTempRepository imageTempRepository;
    private final ContentRepository contentRepository;
    private final ImageUrlExtractor imageUrlExtractor;

    @Value("${img.upload-dir}")
    private String imgDir;

    public String saveImageS3(MultipartFile file) throws IOException {
        String fileUrl = s3Uploader.uploadFile("boards", file);

        imageTempRepository.save(ImageTempEntity.builder()
                .fileUrl(fileUrl)
                .domainType(DomainType.BOARD) 
                .uploadedAt(new Date())
                .build());
        return fileUrl;
    }

    // 전체 이미지 리스트 처리
    @Transactional
    public void saveImagesForBoard(Long boardNo, List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            String fileUrl = s3Uploader.uploadFile("boards", file);
            saveImageEntity(boardNo, file, fileUrl);
        }
    }

    @Transactional
    public void saveImageEntity(Long boardNo, MultipartFile file, String fileUrl) {
        ContentEntity content = contentRepository.findById(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않음: " + boardNo));

        ContentImageEntity image = ContentImageEntity.builder()
            .originalFileName(file.getOriginalFilename())
            .storedFileName(fileUrl.replace("https://crystalrose-web.s3.ap-northeast-2.amazonaws.com/", ""))
            .fileUrl(fileUrl)
            .content(content)
            .build();
        ContentImageEntity saved = contentImageRepository.save(image);
        log.info("이미지 엔티티 저장됨: id={}, fileUrl={}", saved.getId(), saved.getFileUrl());
    }

    @Transactional
    public void saveImages(List<String> imageUrls, ContentEntity content) {
        for (String imageUrl : imageUrls) {
            saveImageAndDeleteTemp(imageUrl, content);
        }
    }

    @Transactional
    public void updateContentImages(String htmlContent, ContentEntity content) {
        List<ContentImageEntity> oldImages = contentImageRepository.findByContent(content);
        for (ContentImageEntity image : oldImages) {
            s3Uploader.deleteFile(image.getFileUrl());
        }
        contentImageRepository.deleteAll(oldImages);

        List<String> imageUrls = imageUrlExtractor.extractImageUrls(htmlContent);
        for (String imageUrl : imageUrls) {
            saveImageAndDeleteTemp(imageUrl, content);
        }
    }

    @Transactional
    public void saveImageAndDeleteTemp(String fileUrl, ContentEntity content) {
        if (!contentImageRepository.existsByFileUrl(fileUrl)) {
            contentImageRepository.save(ContentImageEntity.builder()
                .fileUrl(fileUrl)
                .storedFileName(fileUrl.replace("https://crystalrose-web.s3.ap-northeast-2.amazonaws.com/", ""))
                .content(content)
                .build());
        }
        imageTempRepository.findByFileUrl(fileUrl).ifPresent(imageTempRepository::delete);
    }
}