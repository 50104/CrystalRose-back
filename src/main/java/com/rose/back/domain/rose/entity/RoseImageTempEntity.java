package com.rose.back.domain.rose.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rose_image_temp")
public class RoseImageTempEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileUrl;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedAt;
}
