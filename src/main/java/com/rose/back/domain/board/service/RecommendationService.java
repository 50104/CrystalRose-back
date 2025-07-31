package com.rose.back.domain.board.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.RecommendationEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ContentRepository contentRepository;
    private final RecommendationRepository recommendationRepository;

    public boolean toggleRecommendation(Long boardNo, String userId) {
        ContentEntity content = contentRepository.findByBoardNo(boardNo)
            .orElseThrow(() -> new NoSuchElementException("게시글이 존재하지 않습니다."));

        if (content.getWriter().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인 게시글은 추천할 수 없습니다.");
        }
        Optional<RecommendationEntity> existing = recommendationRepository.findByBoardNoAndUserId(boardNo, userId);

        if (existing.isPresent()) {
            RecommendationEntity rec = existing.get();
            if (Duration.between(rec.getCreatedAt(), LocalDateTime.now()).toMinutes() < 1) {
                throw new IllegalStateException("1분 이내에 추천을 취소할 수 없습니다.");
            }
            recommendationRepository.delete(rec);
            content.decreaseRecommendCount();
            contentRepository.save(content);
            return false; // 추천 취소
        } else {
            recommendationRepository.save(
                RecommendationEntity.builder()
                    .boardNo(boardNo)
                    .userId(userId)
                    .build()
            );
            content.increaseRecommendCount();
            contentRepository.save(content);
            return true; // 추천 추가
        }
    }
}
