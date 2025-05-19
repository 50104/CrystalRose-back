package com.rose.back.domain.board.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ImageEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.ImageRepository;
import com.rose.back.domain.board.repository.ImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;

import java.util.*;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ImageRepository imageRepository;
    private final ImageTempRepository imageTempRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public Long saveContent(ContentRequestDto req) {
        ContentEntity content = new ContentEntity();
        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        content.setUserId(req.getUserId());
        ContentEntity savedContent = contentRepository.save(content);

        Document doc = Jsoup.parse(req.getBoardContent());
        Elements images = doc.select("img");

        for (Element img : images) {
            String imageUrl = img.attr("src");
            log.info("본문 내 이미지 URL: {}", imageUrl);
            if (imageUrl == null || imageUrl.isBlank()) {
                log.warn("이미지 URL이 비어 있음 - 무시됨: {}", img.toString());
                continue;
            }
            saveImageAndDeleteTemp(imageUrl, savedContent);
        }
        return savedContent.getBoardNo();
    }

    public List<ContentEntity> selectContent() {
        return contentRepository.findAll();
    }

    public ContentEntity selectOneContent(Long boardNo) {
        return contentRepository.findByBoardNo(boardNo)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글: " + boardNo));
    }

    @Transactional
    public void deleteOneContent(Long boardNo) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
                .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));
        log.info("게시글 삭제 요청: boardNo = {}", boardNo);

        List<ImageEntity> imageList = imageRepository.findByContent(content); // 해당 게시글의 이미지 목록 조회
        for (ImageEntity image : imageList) { // S3 이미지 삭제
            log.info("S3 이미지 삭제 요청: {}", image.getFileUrl());
            s3Uploader.deleteFile(image.getFileUrl());
        }
        imageRepository.deleteAll(imageList); // 이미지 DB 삭제
        contentRepository.delete(content); // 게시글 삭제
        log.info("게시글 및 연결된 이미지 삭제 완료: boardNo = {}", boardNo);
    }

    public void updateOneContent(ContentRequestDto req, Long boardNo) {
        ContentEntity content = new ContentEntity();
        content.setBoardNo(boardNo);
        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        content.setUserId(req.getUserId());
        contentRepository.save(content);
    }
    
    @Transactional
    public void saveImageAndDeleteTemp(String fileUrl, ContentEntity content) {
        // 중복 저장 방지: 먼저 존재 여부 확인
        if (!imageRepository.existsByFileUrl(fileUrl)) {
            imageRepository.save(ImageEntity.builder()
                .fileUrl(fileUrl)
                .storedFileName(fileUrl.replace("https://cristalrose-web.s3.ap-northeast-2.amazonaws.com/", ""))
                .content(content)
                .build());
            log.info("이미지 DB 저장 완료: {}", fileUrl);
        }else {
            log.info("이미 저장된 이미지, 중복 저장 생략: {}", fileUrl);
        }

        imageTempRepository.findByFileUrl(fileUrl)
            .ifPresent(temp -> {
                imageTempRepository.delete(temp);
                log.info("임시 이미지 DB 삭제 완료: {}", fileUrl);
            });
    }
}