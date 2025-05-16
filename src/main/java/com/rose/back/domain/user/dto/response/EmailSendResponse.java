package com.rose.back.domain.user.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rose.back.common.constants.ResponseCode;
import com.rose.back.common.constants.ResponseMessage;

import lombok.Getter;

@Getter
public class EmailSendResponse extends CommonResponse {
    
    private EmailSendResponse() {
        super();
    }

    public static ResponseEntity<EmailSendResponse> success() {
        EmailSendResponse responseBody = new EmailSendResponse();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<CommonResponse> duplicateId() {
        CommonResponse responseBody = new CommonResponse(ResponseCode.DUPLICATE_ID.getCode(),ResponseMessage.DUPLICATE_ID.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    public static ResponseEntity<CommonResponse> duplicateEmail() {
        CommonResponse responseBody = new CommonResponse(ResponseCode.DUPLICATE_EMAIL.getCode(),ResponseMessage.DUPLICATE_EMAIL.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    public static ResponseEntity<CommonResponse> mailSendFail() {
        CommonResponse responseBody = new CommonResponse(ResponseCode.MAIL_FAIL.getCode(),ResponseMessage.MAIL_FAIL.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }
}
