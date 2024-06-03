package com.rose.back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "certification")
@Table(name = "certification")
public class CertificationEntity {

    @Id
    @Column(length = 100, name = "user_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;

    @Column(length = 100, name = "user_id")
    private String userId;

    @Column(length = 300, name = "user_email")
    private String userEmail;

    @Column(length = 300, name = "certification_number")
    private String certificationNumber;
    
}
