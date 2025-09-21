package com.rose.back.domain.wiki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiWishlistResponse {
  
    private Long id;
    private Long wikiId;
    private String wikiName;
    private String category;
    private String imageUrl;
    private String cultivarCode;
    private String flowerSize;
    private String fragrance;
    private LocalDateTime addedAt;
}