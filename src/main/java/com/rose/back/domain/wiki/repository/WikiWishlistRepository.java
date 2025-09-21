package com.rose.back.domain.wiki.repository;

import com.rose.back.domain.wiki.entity.WikiWishlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WikiWishlistRepository extends JpaRepository<WikiWishlistEntity, Long> {

    // 위키 위시 추가 확인
    @Query("SELECT w FROM WikiWishlistEntity w WHERE w.user.userNo = :userNo AND w.wiki.id = :wikiId")
    Optional<WikiWishlistEntity> findByUserNoAndWikiId(@Param("userNo") Long userNo, @Param("wikiId") Long wikiId);

    // 위키 위시 제거 확인
    @Modifying
    @Query("DELETE FROM WikiWishlistEntity w WHERE w.user.userNo = :userNo AND w.wiki.id = :wikiId")
    void deleteByUserNoAndWikiId(@Param("userNo") Long userNo, @Param("wikiId") Long wikiId);
}