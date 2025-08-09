package com.rose.back.domain.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rose.back.domain.comment.entity.CommentEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
  
    List<CommentEntity> findByContentEntityBoardNo(Long boardNo);

    long countByContentEntity_BoardNo(Long boardNo);

    @Query("""
        select c.contentEntity.boardNo, count(c)
        from CommentEntity c
        where c.contentEntity.boardNo in :boardNos
        group by c.contentEntity.boardNo
    """)
    List<Object[]> countCommentsByBoardNos(@Param("boardNos") List<Long> boardNos);
}