package com.rose.back.domain.wiki.service;

import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.domain.wiki.dto.WikiWishlistAddRequest;
import com.rose.back.domain.wiki.dto.WikiWishlistResponse;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiWishlistEntity;
import com.rose.back.domain.wiki.repository.WikiRepository;
import com.rose.back.domain.wiki.repository.WikiWishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WikiWishlistService {

    private final WikiWishlistRepository wishlistRepository;
    private final WikiRepository wikiRepository;
    private final UserRepository userRepository;

    @Transactional
    public WikiWishlistResponse addToWishlist(Long userNo, WikiWishlistAddRequest request) {
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        WikiEntity wiki = wikiRepository.findById(request.getWikiId())
                .orElseThrow(() -> new IllegalArgumentException("위키를 찾을 수 없습니다."));

        if (wishlistRepository.findByUserNoAndWikiId(userNo, request.getWikiId()).isPresent()) {
            throw new IllegalStateException("이미 위시리스트에 추가된 장미입니다.");
        }

        WikiWishlistEntity wishlistEntity = WikiWishlistEntity.builder()
                .user(user)
                .wiki(wiki)
                .build();
        WikiWishlistEntity savedEntity = wishlistRepository.save(wishlistEntity);

        return convertToResponse(savedEntity);
    }

    @Transactional
    public void removeFromWishlist(Long userNo, Long wikiId) {
        WikiWishlistEntity wishlistEntity = wishlistRepository.findByUserNoAndWikiId(userNo, wikiId)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트에서 해당 장미를 찾을 수 없습니다."));

        wishlistRepository.delete(wishlistEntity);
    }

    private WikiWishlistResponse convertToResponse(WikiWishlistEntity entity) {
        return WikiWishlistResponse.builder()
                .id(entity.getId())
                .wikiId(entity.getWiki().getId())
                .wikiName(entity.getWiki().getName())
                .category(entity.getWiki().getCategory())
                .imageUrl(entity.getWiki().getImageUrl())
                .cultivarCode(entity.getWiki().getCultivarCode())
                .flowerSize(entity.getWiki().getFlowerSize())
                .fragrance(entity.getWiki().getFragrance())
                .addedAt(entity.getCreatedDate())
                .build();
    }
}