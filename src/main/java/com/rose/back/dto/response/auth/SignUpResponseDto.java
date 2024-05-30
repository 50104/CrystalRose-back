package com.rose.back.dto.response.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rose.back.common.ResponseCode;
import com.rose.back.common.ResponseMessage;
import com.rose.back.dto.response.ResponseDto;

import lombok.Getter;

@Getter
public class SignUpResponseDto extends ResponseDto{
    
    private SignUpResponseDto() {

        super();
    }

    public static ResponseEntity<SignUpResponseDto> success() {

        SignUpResponseDto responseBody = new SignUpResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> duplicateId() {

        ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> certificationFail() {

        ResponseDto responseBody = new ResponseDto(ResponseCode.CERTIFICATION_FAIL, ResponseMessage.CERTIFICATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}
