package com.rose.back.domain.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity(name = "board_content")
@Table(name = "board_content")
public class ContentEntity {

    @Id
    @Column(length = 50, name = "board_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardNo;

    @Column(length = 100, name = "board_title")
    private String boardTitle;

    @Column(columnDefinition = "LONGTEXT", name = "board_content")
    private String boardContent;

    @Column(length = 50, name = "user_id")
    private String userId;
}