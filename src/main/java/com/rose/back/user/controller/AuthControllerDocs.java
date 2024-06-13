package com.rose.back.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.rose.back.user.dto.request.auth.CheckCertificationRequestDto;
import com.rose.back.user.dto.request.auth.EmailCertificationRequestDto;
import com.rose.back.user.dto.request.auth.IdCheckRequestDto;
import com.rose.back.user.dto.request.auth.SignInRequestDto;
import com.rose.back.user.dto.request.auth.SignUpRequestDto;
import com.rose.back.user.dto.response.auth.CheckCertificationResponseDto;
import com.rose.back.user.dto.response.auth.EmailCertificationResponseDto;
import com.rose.back.user.dto.response.auth.IdCheckResponseDto;
import com.rose.back.user.dto.response.auth.SignInResponseDto;
import com.rose.back.user.dto.response.auth.SignUpResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Auth", description = "Auth 관련 API입니다.")
public interface AuthControllerDocs {

        @Operation(summary = "아이디 중복 체크", description = "아이디 중복 여부를 체크합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "아이디 중복 체크 성공"),
        @ApiResponse(responseCode = "409", description = "아이디 중복 체크 실패(중복된 아이디)")})
        public ResponseEntity<? super IdCheckResponseDto> idCheck(@RequestBody @Valid IdCheckRequestDto requestBody);

        @Operation(summary = "이메일 인증", description = "이메일 인증 여부를 확인합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 인증 확인 성공"),
        @ApiResponse(responseCode = "409", description = "이메일 인증 확인 실패(중복된 아이디)")})
        public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(@RequestBody @Valid EmailCertificationRequestDto requestBody);

        @Operation(summary = "이메일 인증 번호 확인", description = "이메일 인증번호를 체크합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 번호인증 성공"),
        @ApiResponse(responseCode = "409", description = "이메일 번호인증 실패(다른 인증번호)")})
        public ResponseEntity<? super CheckCertificationResponseDto> checkCertification(@RequestBody @Valid CheckCertificationRequestDto requestBody);

        @Operation(summary = "회원가입", description = "회원가입을 실행합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "409", description = "회원가입 실패")})
        public ResponseEntity<? super SignUpResponseDto> signUp(@RequestBody @Valid SignUpRequestDto requestBody);

        @Operation(summary = "로그인", description = "로그인을 실행합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "409", description = "로그인 실패")})
        public ResponseEntity<? super SignInResponseDto> signIn(@RequestBody @Valid SignInRequestDto requestBody);

        @Operation(summary = "로그아웃", description = "로그아웃을 실행합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "409", description = "로그아웃 실패")})
        public ResponseEntity<String> logout(@RequestBody @Valid HttpServletRequest request, HttpServletResponse response);
}
