package com.rose.back.user.entity;

import java.time.LocalDate;

import com.rose.back.user.dto.request.auth.SignUpRequestDto;

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
@Entity(name = "user_list")
@Table(name = "user_list")
public class UserEntity {
    
    //@Entity(name="user_list") UserEntity를 엔터티 클래스로 사용하겠다 jpa이름은 유저리스트
    //@Table(name = "user_list") 데이터베이스의 유저리스트 라는 테이블과 맵핑하겠다

    @Id
    @Column(length = 50, name = "user_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNo;
    
    @Column(length = 50, name = "user_id")
    private String userId;

    @Column(length = 300, name = "user_email")
    private String userEmail;

    @Column(length = 300, name = "user_pwd")
    private String userPwd;

    @Column(length = 50, name = "user_name")
    private String userName;

    @Column(length = 50, name = "user_type")
    private String userType;

    @Column(length = 50, name = "user_role")
    private String userRole;

    @Column(length = 50, name = "user_tel")
    private String userTel;

    @Column(length = 100, name = "user_profile")
    private String userProfile;

    @Column(length = 50, name = "user_nick")
    private String userNick;

    // 회원가입을 위한 생성자
    public UserEntity (SignUpRequestDto dto) {

        this.userId = dto.getUserId();
        this.userPwd = dto.getUserPwd();
        this.userEmail = dto.getUserEmail();
        this.userType ="web";
        this.userRole = "ROLE_USER";
        this.apDate = LocalDate.now();
    }

    // 소셜 로그인을 위한 생성자
    public UserEntity(String userId, String UserEmail, String type) {
        this.userId = userId;
        this.userPwd = null;
        this.userEmail = UserEmail;
        this.userType = type;
        this.userRole = "ROLE_USER";
        this.apDate = LocalDate.now();
    }

    @Column(name = "ap_date")
    private LocalDate apDate = LocalDate.now();
}
