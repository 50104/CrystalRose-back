package com.rose.back.user.dto;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long userNo;
    @JsonProperty("id")// id는 username으로 변경
    private String userName;

    private String userEmail;
    private String userPwd;
    private String userType;
    private String userRole;
    private String userProfileImg;
    @JsonIgnore// password는 보안상 노출되면 안되므로 json으로 변환하지 않음
    private MultipartFile userProfileFile;
    private String userNick;
    private String isDelete;
    private String apDate;
}
