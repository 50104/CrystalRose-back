package com.rose.back.domain.report.entity;

import java.time.LocalDateTime;

import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.user.entity.UserEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "report_comment")
public class CommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    private CommentEntity targetComment;

    private String reason;

    private LocalDateTime reportedAt = LocalDateTime.now();
}
