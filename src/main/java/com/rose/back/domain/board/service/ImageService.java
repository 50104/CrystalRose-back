package com.rose.back.domain.board.service;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ImageEntity;
import com.rose.back.domain.board.entity.ImageTempEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.ImageRepository;
import com.rose.back.domain.board.repository.ImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;

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
public class ImageService {
  
    private final S3Uploader s3Uploader;
    private final ImageTempRepository imageTempRepository;
    private final ImageRepository imageRepository;
    private final ContentRepository contentRepository;

    @Value("${img.upload-dir}")
    private String imgDir;

    public String saveImageS3(MultipartFile file, Long boardNo) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("파일이 없습니다.");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new IOException("파일명 오류: 확장자가 없습니다.");
        }
        try {
            String fileUrl = s3Uploader.uploadFile(file, boardNo);

            imageTempRepository.save(ImageTempEntity.builder()
                .fileUrl(fileUrl)
                .uploadedAt(new Date())
                .build());

            return fileUrl;
        } catch (Exception e) {
            log.error("S3 업로드 실패", e); // 전체 스택 출력
            throw new IOException("S3 업로드 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void saveImageEntity(Long boardNo, MultipartFile file, String fileUrl) {
        ContentEntity content = contentRepository.findById(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않음: " + boardNo));

        ImageEntity image = ImageEntity.builder()
            .originalFileName(file.getOriginalFilename())
            .storedFileName(fileUrl.replace("https://crystalrose-web.s3.ap-northeast-2.amazonaws.com/", ""))
            .fileUrl(fileUrl)
            .content(content)
            .build();
        ImageEntity saved = imageRepository.save(image);
        log.info("이미지 엔티티 저장됨: id={}, fileUrl={}", saved.getId(), saved.getFileUrl());
    }

    // 전체 이미지 리스트 처리
    @Transactional
    public void saveImagesForBoard(Long boardNo, List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            String fileUrl = s3Uploader.uploadFile(file, boardNo);
            saveImageEntity(boardNo, file, fileUrl);
        }
    }
}