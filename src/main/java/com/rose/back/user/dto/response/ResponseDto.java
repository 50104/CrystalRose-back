package com.rose.back.user.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rose.back.user.common.ResponseCode;
import com.rose.back.user.common.ResponseMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto {
    
    private String code; // 응답 코드를 저장하는 변수
    private String message; // 응답 메시지를 저장하는 변수

    public ResponseDto() {
        this.code = ResponseCode.SUCCESS.getCode(); // 응답 코드를 성공 코드로 초기화
        this.message = ResponseMessage.SUCCESS.getMessage(); // 응답 메시지를 성공 메시지로 초기화
    }

    public static ResponseEntity<ResponseDto> databaseError() { // 데이터베이스 오류 응답을 생성하는 메소드
        ResponseDto responseBody = new ResponseDto(ResponseCode.DATABASE_ERROR.getCode(), ResponseMessage.DATABASE_ERROR.getMessage()); // 응답 객체 생성 및 응답 코드와 메시지 설정       
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody); // 500 Internal Server Error 상태 코드와 응답 객체를 포함한 ResponseEntity 반환       
    }

    public static ResponseEntity<ResponseDto> validationFail() { // 유효성 검사 실패 응답을 생성하는 메소드
        ResponseDto responseBody = new ResponseDto(ResponseCode.VALIDATION_FAIL.getCode(), ResponseMessage.VALIDATION_FAIL.getMessage()); // 응답 객체 생성 및 응답 코드와 메시지 설정
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody); // 400 Bad Request 상태 코드와 응답 객체를 포함한 ResponseEntity 반환      
    }
}