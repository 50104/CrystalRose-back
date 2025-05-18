package com.rose.back.domain.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "board_image_temp")
@Table(name = "board_image_temp")
public class ImageTempEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileUrl;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedAt;
}