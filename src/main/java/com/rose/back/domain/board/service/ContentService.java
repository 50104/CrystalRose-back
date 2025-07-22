package com.rose.back.domain.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.dto.ContentListDto;
import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.dto.ContentWithWriterDto;
import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ContentImageEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.comment.repository.CommentRepository;
import com.rose.back.domain.board.repository.ContentImageRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.domain.user.service.UserService;
import com.rose.back.infra.S3.ImageUrlExtractor;
import com.rose.back.infra.S3.S3Uploader;

import java.util.*;

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
    private final UserService userService;
    private final CommentRepository commentRepository;

    @Transactional
    public Long saveContent(ContentRequestDto req) {
        ContentEntity content = new ContentEntity();
        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        content.setBoardTag(req.getBoardTag());

        UserEntity user = userRepository.findByUserId(req.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");
        }

        if ("공지".equalsIgnoreCase(req.getBoardTag()) && !userService.isAdmin(req.getUserId())) {
            throw new AccessDeniedException("공지 작성 권한이 없습니다.");
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

    public ContentWithWriterDto selectOneContentDto(Long boardNo) { // 상세
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));
        long commentCount = commentRepository.countByContentEntity_BoardNo(boardNo);

        return ContentWithWriterDto.from(content, commentCount);
    }

    public Page<ContentListDto> selectContentPage(int page, int size) { // 목록
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "boardNo"));

        return contentRepository.findAll(pageable)
            .map(entity -> {
                long commentCount = commentRepository.countByContentEntity_BoardNo(entity.getBoardNo());
                return ContentListDto.from(entity, commentCount);
            });
    }

    @Transactional
    public void deleteOneContent(Long boardNo, String username) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
                .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));

        if (!content.getWriter().getUserId().equals(username)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }
        log.info("게시글 삭제 요청: boardNo = {}", boardNo);

        List<ContentImageEntity> imageList = contentImageRepository.findByContent(content);

        for (ContentImageEntity image : imageList) {
            log.info("S3 이미지 삭제 요청: {}", image.getFileUrl());
            s3Uploader.deleteFile(image.getFileUrl());
        }

        contentImageRepository.deleteAll(imageList);
        contentRepository.delete(content);
        log.info("게시글 및 연결된 이미지 삭제 완료: boardNo = {}", boardNo);
    }

    @Transactional
    public void updateOneContent(ContentRequestDto req, Long boardNo) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));

        if (!content.getWriter().getUserId().equals(req.getUserId())) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        content.setBoardTag(req.getBoardTag());

        UserEntity user = userRepository.findByUserId(req.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");
        }

        content.setWriter(user);
        contentRepository.save(content);

        contentImageService.updateContentImages(req.getBoardContent(), content);
    }
}
