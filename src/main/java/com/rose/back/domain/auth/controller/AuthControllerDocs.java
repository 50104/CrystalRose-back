package com.rose.back.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.EmailVerifyRequest;
import com.rose.back.domain.user.dto.request.EmailSendRequest;
import com.rose.back.domain.user.dto.request.IdCheckRequest;
import com.rose.back.domain.user.dto.response.EmailVerifyResponse;
import com.rose.back.domain.user.dto.response.EmailSendResponse;
import com.rose.back.domain.user.dto.response.IdCheckResponse;
import com.rose.back.domain.user.dto.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Auth", description = "Auth 관련 API입니다.")
public interface AuthControllerDocs {

        @Operation(summary = "아이디 중복 체크", description = "아이디 중복 여부를 체크합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "아이디 중복 체크 성공"),
        @ApiResponse(responseCode = "409", description = "아이디 중복 체크 실패(중복된 아이디)")})
        public ResponseEntity<? super IdCheckResponse> idCheck(@RequestBody @Valid IdCheckRequest requestBody);

        @Operation(summary = "이메일 중복 확인", description = "이메일 중복 여부를 확인합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 중복 확인 성공"),
        @ApiResponse(responseCode = "409", description = "이메일 중복 확인 실패(중복된 이메일)")})
        public ResponseEntity<? super EmailSendResponse> checkEmail(@RequestBody @Valid EmailSendRequest requestBody);

        @Operation(summary = "이메일 인증", description = "이메일 인증 여부를 확인합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 인증 확인 성공"),
        @ApiResponse(responseCode = "409", description = "이메일 인증 확인 실패(중복된 아이디)")})
        public ResponseEntity<? super EmailSendResponse> emailCertification(@RequestBody @Valid EmailSendRequest requestBody);

        @Operation(summary = "이메일 인증 번호 확인", description = "이메일 인증번호를 체크합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 번호인증 성공"),
        @ApiResponse(responseCode = "409", description = "이메일 번호인증 실패(다른 인증번호)")})
        public ResponseEntity<? super EmailVerifyResponse> checkCertification(@RequestBody @Valid EmailVerifyRequest requestBody);

        @Operation(summary = "회원가입", description = "회원가입을 실행합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "409", description = "회원가입 실패")})
        public ResponseEntity<? super CommonResponse> join(@RequestBody UserInfoDto userDto, BindingResult bindingResult);

        @Operation(summary = "아이디 찾기", description = "이메일로 아이디를 찾습니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
        @ApiResponse(responseCode = "409", description = "아이디 찾기 실패")})
        public ResponseEntity<?> findUserId(@RequestBody EmailSendRequest request);
        
        @Operation(summary = "비밀번호 초기화", description = "이메일과 아이디로 비밀번호를 초기화합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 초기화 성공"),
        @ApiResponse(responseCode = "400", description = "비밀번호 초기화 실패")})
        public ResponseEntity<?> findUserPwd(@RequestBody EmailSendRequest request);
}