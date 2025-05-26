package com.rose.back.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.PwdValidationRequest;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.exception.ErrorResponse;
import com.rose.back.global.exception.ImageUploadErrorResponses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@Tag(name = "User", description = "User 관련 API입니다.")
public interface UserControllerDocs {

    @Operation(summary = "사용자 정보", description = "사용자 정보를 불러옵니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 불러오기 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "사용자 정보 불러오기 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "사용자 정보를 불러오지 못했습니다.",
                      "path": "/api/v1/user"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> get();

    @Operation(summary = "비밀번호 확인", description = "비밀번호를 확인합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 확인 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "비밀번호 확인 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "비밀번호가 일치하지 않습니다.",
                      "path": "/api/v1/user/validate-password"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> validatePassword(PwdValidationRequest request);

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "사용자 정보 수정 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "사용자 정보 수정에 실패했습니다.",
                      "path": "/api/v1/user"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> updateUser(UserInfoDto user);

    @Operation(summary = "프로필 이미지 변경", description = "프로필 이미지를 변경합니다.")
    @CommonErrorResponses
    @ImageUploadErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 이미지 변경 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "프로필 이미지 변경 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "프로필 이미지 변경에 실패했습니다.",
                      "path": "/api/v1/user/profile-image"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> modify(@ModelAttribute UserInfoDto userDTO);

    @Operation(summary = "회원 목록", description = "회원 목록을 불러옵니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 목록 불러오기 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "회원 목록 불러오기 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "회원 목록을 불러오지 못했습니다.",
                      "path": "/api/v1/user/members"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> memberList();
}