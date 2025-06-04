package com.rose.back.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.report.entity.CommentReport;
import com.rose.back.domain.user.entity.UserEntity;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
  
    boolean existsByReporterAndTargetComment(UserEntity reporter, CommentEntity comment);
}
