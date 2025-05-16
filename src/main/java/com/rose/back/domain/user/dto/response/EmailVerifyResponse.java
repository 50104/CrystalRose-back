package com.rose.back.domain.user.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rose.back.common.constants.ResponseCode;
import com.rose.back.common.constants.ResponseMessage;

import lombok.Getter;

@Getter
public class EmailVerifyResponse extends CommonResponse {
    
    private EmailVerifyResponse() {
        super();
    }

    public static ResponseEntity<EmailVerifyResponse> success() {
        EmailVerifyResponse responseBody = new EmailVerifyResponse();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<CommonResponse> certificationFail() {
        CommonResponse responseBody = new CommonResponse(ResponseCode.CERTIFICATION_FAIL.getCode(), ResponseMessage.CERTIFICATION_FAIL.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}
