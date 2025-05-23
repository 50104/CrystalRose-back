package com.rose.back.domain.report.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.report.entity.Report;
import com.rose.back.domain.user.entity.UserEntity;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByTargetUser(UserEntity targetUser);
    
    List<Report> findAllByReporter(UserEntity reporter);

    boolean existsByReporterAndTargetPost(UserEntity reporter, ContentEntity targetPost);

    boolean existsByReporter_UserNoAndTargetPost_BoardNo(Long reporterId, Long postId);
}
