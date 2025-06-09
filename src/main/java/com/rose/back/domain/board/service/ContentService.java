package com.rose.back.domain.board.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.dto.ContentListDto;
import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.dto.ContentWithWriterDto;
import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ContentImageEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.ContentImageRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.infra.S3.ImageUrlExtractor;
import com.rose.back.infra.S3.S3Uploader;

import java.util.*;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentImageService contentImageService;
    private final ContentImageRepository contentImageRepository;
    private final ImageUrlExtractor imageUrlExtractor;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;

    @Transactional
    public Long saveContent(ContentRequestDto req) {
        ContentEntity content = new ContentEntity();
        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        UserEntity user = userRepository.findByUserId(req.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");
        }
        content.setWriter(user);
        ContentEntity savedContent = contentRepository.save(content);

        List<String> imageUrls = imageUrlExtractor.extractImageUrls(req.getBoardContent());
        contentImageService.saveImages(imageUrls, savedContent);

        return savedContent.getBoardNo();
    }

    public List<ContentEntity> selectContent() {
        return contentRepository.findAll();
    }

    public ContentWithWriterDto selectOneContentDto(Long boardNo) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));
        return ContentWithWriterDto.from(content);
    }


    @Transactional
    public void deleteOneContent(Long boardNo) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
                .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));
        log.info("게시글 삭제 요청: boardNo = {}", boardNo);

        List<ContentImageEntity> imageList = contentImageRepository.findByContent(content); // 해당 게시글의 이미지 목록 조회
        for (ContentImageEntity image : imageList) { // S3 이미지 삭제
            log.info("S3 이미지 삭제 요청: {}", image.getFileUrl());
            s3Uploader.deleteFile(image.getFileUrl());
        }
        contentImageRepository.deleteAll(imageList); // 이미지 DB 삭제
        contentRepository.delete(content); // 게시글 삭제
        log.info("게시글 및 연결된 이미지 삭제 완료: boardNo = {}", boardNo);
    }

    @Transactional
    public void updateOneContent(ContentRequestDto req, Long boardNo) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));

        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());

        UserEntity user = userRepository.findByUserId(req.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");
        }
        content.setWriter(user);
        contentRepository.save(content);

        contentImageService.updateContentImages(req.getBoardContent(), content);
    }

    public Page<ContentListDto> selectContentPage(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "boardNo"));
        return contentRepository.findAll(pageable)
            .map(ContentListDto::from);
    }
}