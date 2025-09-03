package com.rose.back.domain.board.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.RecommendationEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ContentRepository contentRepository;
    private final RecommendationRepository recommendationRepository;

    @Transactional
    public boolean toggleRecommendation(Long boardNo, String userId) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다."));

        if (content.getWriter().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인 게시글은 추천할 수 없습니다.");
        }

        // 기존 추천 여부 확인
        boolean hasExistingRecommendation = recommendationRepository.findByBoardNoAndUserId(boardNo, userId).isPresent();

        if (hasExistingRecommendation) {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1); // 1분 제한
            
            // 1분 이전에 생성된 추천만 삭제
            int deletedRows = recommendationRepository.deleteByBoardNoAndUserIdAndCreatedAtBefore(
                boardNo, userId, oneMinuteAgo);
            
            if (deletedRows == 0) {
                throw new IllegalStateException("1분 이내에 추천을 취소할 수 없습니다.");
            }
            
            int updatedRows = contentRepository.decrementRecommendCount(boardNo); // 추천 수 감소
            
            if (updatedRows == 0) {
                log.warn("게시글 {}의 추천 수 감소 실패", boardNo);
                throw new IllegalStateException("추천 수 업데이트에 실패했습니다."); // 추천 삭제 롤백위한 예외
            }
            
            log.info("추천 취소 완료 - 게시글: {}, 사용자: {}", boardNo, userId);
            return false; // 추천 취소
            
        } else {
            try {
                RecommendationEntity recommendation = RecommendationEntity.builder() // 추천 정보 저장
                    .boardNo(boardNo)
                    .userId(userId)
                    .build();
                recommendationRepository.save(recommendation);
                
                int updatedRows = contentRepository.incrementRecommendCount(boardNo); // 추천 수 증가
                
                if (updatedRows == 0) {
                    log.warn("게시글 {}의 추천 수 증가 실패", boardNo);
                    throw new IllegalStateException("추천 수 업데이트에 실패했습니다.");
                }
                
                log.info("추천 추가 완료 - 게시글: {}, 사용자: {}", boardNo, userId);
                return true; // 추천 추가
                
            } catch (DataIntegrityViolationException e) {
                log.warn("중복 추천 시도 감지 - 게시글: {}, 사용자: {}", boardNo, userId);
                throw new IllegalStateException("이미 추천한 게시글입니다.");
            }
        }
    }
}
