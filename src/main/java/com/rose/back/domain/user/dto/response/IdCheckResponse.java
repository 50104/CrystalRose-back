package com.rose.back.domain.user.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rose.back.common.constants.ResponseCode;
import com.rose.back.common.constants.ResponseMessage;

import lombok.Getter;

@Getter
public class IdCheckResponse extends CommonResponse {
    
    private IdCheckResponse() {
        super();
    }

    public static ResponseEntity<IdCheckResponse> success() {
        IdCheckResponse responseBody = new IdCheckResponse();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<CommonResponse> duplicateId() {
        CommonResponse responseBody = new CommonResponse(ResponseCode.DUPLICATE_ID.getCode(), ResponseMessage.DUPLICATE_ID.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
