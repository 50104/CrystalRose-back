package com.rose.back.domain.auth.controller.docs;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.EmailVerifyRequest;
import com.rose.back.domain.user.dto.request.EmailSendRequest;
import com.rose.back.domain.user.dto.request.IdCheckRequest;
import com.rose.back.domain.user.dto.response.EmailVerifyResponse;
import com.rose.back.domain.user.dto.response.EmailSendResponse;
import com.rose.back.domain.user.dto.response.IdCheckResponse;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.domain.user.dto.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import com.rose.back.global.exception.ErrorResponse;
import com.rose.back.global.exception.JwtReissueErrorResponses;

@Tag(name = "Auth", description = "Auth 관련 API입니다.")
public interface AuthControllerDocs {

    @Operation(summary = "아이디 중복 체크", description = "아이디 중복 여부를 체크합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "아이디 중복 체크 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "아이디 중복 체크 실패(중복된 아이디)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미 사용 중인 아이디입니다.",
                      "path": "/api/v1/auth/id-check"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<? super IdCheckResponse> idCheck(@RequestBody @Valid IdCheckRequest requestBody);

    @Operation(summary = "이메일 중복 확인", description = "이메일 중복 여부를 확인합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 중복 확인 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "이메일 중복 확인 실패(중복된 이메일)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미 사용 중인 이메일입니다.",
                      "path": "/api/v1/auth/email-check"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<? super EmailSendResponse> checkEmail(@RequestBody @Valid EmailSendRequest requestBody);

    @Operation(summary = "이메일 인증", description = "이메일 인증 여부를 확인합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 인증 확인 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "이메일 인증 확인 실패(중복된 아이디)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미 인증된 아이디입니다.",
                      "path": "/api/v1/auth/email-certification"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<? super EmailSendResponse> emailCertification(@RequestBody @Valid EmailSendRequest requestBody);

    @Operation(summary = "이메일 인증 번호 확인", description = "이메일 인증번호를 체크합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 번호인증 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "이메일 번호인증 실패(다른 인증번호)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "인증번호가 일치하지 않습니다.",
                      "path": "/api/v1/auth/email-certification-check"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<? super EmailVerifyResponse> checkCertification(@RequestBody @Valid EmailVerifyRequest requestBody);

    @Operation(summary = "회원가입", description = "회원가입을 실행합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "회원가입 실패(이미 존재하는 아이디/이메일)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미 존재하는 아이디 또는 이메일입니다.",
                      "path": "/api/v1/auth/join"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<? super CommonResponse> join(@RequestBody UserInfoDto userDto, BindingResult bindingResult);

    @Operation(summary = "아이디 찾기", description = "이메일로 아이디를 찾습니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "아이디 찾기 실패(일치하는 정보 없음)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "일치하는 정보가 없습니다.",
                      "path": "/api/v1/auth/find-id"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> findUserId(@RequestBody EmailSendRequest request);

    @Operation(summary = "비밀번호 초기화", description = "이메일과 아이디로 비밀번호를 초기화합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 초기화 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "비밀번호 초기화 실패(일치하는 정보 없음)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "일치하는 정보가 없습니다.",
                      "path": "/api/v1/auth/find-pwd"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> findUserPwd(@RequestBody EmailSendRequest request);

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴(회원 정보 삭제 및 토큰 무효화)를 수행합니다.")
    @JwtReissueErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공")
    })
    ResponseEntity<?> withdraw(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> body);
}