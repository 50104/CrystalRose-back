package com.rose.back.domain.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.dto.ContentListDto;
import com.rose.back.domain.board.dto.ContentListResponse;
import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.dto.ContentSummaryDto;
import com.rose.back.domain.board.dto.ContentWithWriterDto;
import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ContentImageEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.RecommendationRepository;
import com.rose.back.domain.comment.repository.CommentRepository;
import com.rose.back.domain.report.repository.UserBlockRepository;
import com.rose.back.domain.board.repository.ContentImageRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.domain.user.service.UserService;
import com.rose.back.infra.S3.ImageUrlExtractor;
import com.rose.back.infra.S3.S3Uploader;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserBlockRepository userBlockRepository;
    private final ContentImageService contentImageService;
    private final ContentImageRepository contentImageRepository;
    private final ImageUrlExtractor imageUrlExtractor;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final RedisViewService redisViewService;
    private final RecommendationRepository recommendationRepository;

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

    // 게시글 상세 조회
    @Transactional
    public ContentWithWriterDto selectOneContentDto(Long boardNo, String currentUserId, boolean increaseViewCount) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다: " + boardNo));

        List<UserEntity> blockedUsers = userBlockRepository.findAllBlockedByUserId(currentUserId);
        if (blockedUsers.contains(content.getWriter())) {
            throw new AccessDeniedException("차단한 사용자의 게시글입니다.");
        }

        if (increaseViewCount && currentUserId != null) {
            String writerId = Optional.ofNullable(content.getWriter()).map(UserEntity::getUserId).orElse(null);
            boolean isWriter = writerId != null && writerId.equals(currentUserId);
            if (isWriter) {
                if (!redisViewService.isDuplicateView(currentUserId, boardNo)) {
                    content.setViewCount(content.getViewCount() + 1);
                }
            } else {
                content.setViewCount(content.getViewCount() + 1); // 타인은 무조건 증가
            }
        }

        long commentCount = commentRepository.countByContentEntity_BoardNo(boardNo);
        long likeCount = recommendationRepository.countByBoardNo(boardNo);
        boolean recommended = currentUserId != null &&
            recommendationRepository.findByBoardNoAndUserId(boardNo, currentUserId).isPresent();

        return ContentWithWriterDto.from(content, commentCount, likeCount, recommended);
    }

    public ContentWithWriterDto selectOneContentDto(Long boardNo) {
        return selectOneContentDto(boardNo, null, false);
    }

    public Page<ContentListDto> selectContentPage(int page, int size, String currentUserId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "boardNo"));
        List<UserEntity> blockedUsers = userBlockRepository.findAllBlockedByUserId(currentUserId);

        Page<ContentEntity> all = contentRepository.findAll(pageable);
        List<ContentListDto> filtered = all.stream()
            .filter(content -> !blockedUsers.contains(content.getWriter()))
            .map(content -> {
                long commentCount = commentRepository.countByContentEntity_BoardNo(content.getBoardNo());
                return ContentListDto.from(content, commentCount);
            }).toList();

        return new PageImpl<>(filtered, pageable, filtered.size());
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

    @Transactional
    public boolean toggleFixed(Long boardId) {
        ContentEntity content = contentRepository.findById(boardId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        boolean newState = !content.isFixed();
        if (newState) {
            long fixedCount = contentRepository.countByIsFixedTrue();
            if (fixedCount >= 10) {
                throw new IllegalStateException("고정 가능한 게시글은 최대 10개입니다.");
            }
        }
        content.setFixed(newState);
        return newState;
    }

    public ContentListResponse selectContentPageWithFixed(int page, int size, String userId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "boardNo"));

        // 차단한 사용자 번호 리스트
        Set<Long> blockedUserNos = (userId != null)
            ? userBlockRepository.findAllBlockedByUserId(userId).stream()
                .map(UserEntity::getUserNo)
                .collect(Collectors.toSet())
            : Set.of(); // 비로그인 사용자는 차단 목록 없음
        log.info("차단 대상 userNos = {}", blockedUserNos); 

        // 고정 게시글 조회 후 차단 사용자 필터링
        List<ContentListDto> fixedList = contentRepository.findByIsFixedTrueOrderByBoardNoDesc().stream()
            .filter(content -> content.getWriter() != null &&
                              !blockedUserNos.contains(content.getWriter().getUserNo()))
            .map(content -> {
                long commentCount = commentRepository.countByContentEntity_BoardNo(content.getBoardNo());
                return ContentListDto.from(content, commentCount);
            })
            .toList();

        // 일반 게시글 조회 후 차단 사용자 필터링
        Page<ContentEntity> contentPage = contentRepository.findByIsFixedFalse(pageable);
        List<ContentListDto> contentList = contentPage.getContent().stream()
            .filter(content -> content.getWriter() != null &&
                              !blockedUserNos.contains(content.getWriter().getUserNo()))
            .map(content -> {
                long commentCount = commentRepository.countByContentEntity_BoardNo(content.getBoardNo());
                return ContentListDto.from(content, commentCount);
            })
            .toList();

        return new ContentListResponse(fixedList, contentList, contentPage.getTotalPages());
    }

    public Optional<ContentSummaryDto> getPreviousPost(Long boardNo) {
        return contentRepository.findPrevPost(boardNo, PageRequest.of(0, 1)).stream()
                .findFirst()
                .map(ContentSummaryDto::from);
    }

    public Optional<ContentSummaryDto> getNextPost(Long boardNo) {
        return contentRepository.findNextPost(boardNo, PageRequest.of(0, 1)).stream()
                .findFirst()
                .map(ContentSummaryDto::from);
    }
}
