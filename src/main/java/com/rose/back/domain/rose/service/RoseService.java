package com.rose.back.domain.rose.service;

import org.springframework.stereotype.Service;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.rose.controller.RoseController;
import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.domain.rose.repository.RoseRepository;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoseService {

    private final RoseRepository userRoseRepository;
    private final WikiRepository roseWikiRepository;
    private final RoseImageService roseImageService;

    public void registerUserRose(CustomUserDetails userDetails, RoseController.RoseRequest request) {
        Long userId = userDetails.getUserNo();
        WikiEntity roseWiki = roseWikiRepository.findById(request.wikiId())
            .orElseThrow(() -> new IllegalArgumentException("도감 품종이 존재하지 않습니다"));

        RoseEntity userRose = RoseEntity.builder()
            .wikiEntity(roseWiki)
            .userId(userId)
            .nickname(request.nickname())
            .acquiredDate(request.acquiredDate())
            .locationNote(request.locationNote())
            .imageUrl(request.imageUrl())
            .build();

        userRoseRepository.save(userRose);

        // 이미지 엔티티 저장 및 temp 삭제
        roseImageService.saveImageEntityAndDeleteTemp(
            request.imageUrl(),
            null, // 파일 이름을 클라이언트에서 함께 보낼 수 있다면 전달
            userRose
        );
    }
}
