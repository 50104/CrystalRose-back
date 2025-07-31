package com.rose.back.domain.board.entity;

import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "board_content")
public class ContentEntity extends BaseTimeEntity {

    @Id
    @Column(length = 50, name = "board_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardNo;

    @Column(length = 100, name = "board_title")
    private String boardTitle;

    @Column(length = 30, name = "board_tag")
    private String boardTag;

    @Column(columnDefinition = "LONGTEXT", name = "board_content")
    private String boardContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity writer;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
}