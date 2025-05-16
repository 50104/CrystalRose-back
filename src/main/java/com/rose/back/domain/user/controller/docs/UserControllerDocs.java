package com.rose.back.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.PwdValidationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "User", description = "User 관련 API입니다.")
public interface UserControllerDocs {

        @Operation(summary = "사용자 정보", description = "사용자 정보를 불러옵니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 불러오기 성공"),
        @ApiResponse(responseCode = "409", description = "사용자 정보 불러오기 실패")})
        public ResponseEntity<?> get();

        @Operation(summary = "비밀번호 확인", description = "비밀번호를 확인합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 확인 성공"),
        @ApiResponse(responseCode = "409", description = "비밀번호 확인 실패")})
        public ResponseEntity<Boolean> validatePassword(PwdValidationRequest request);

        @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
        @ApiResponse(responseCode = "409", description = "사용자 정보 수정 실패")})
        public ResponseEntity<?> updateUser( UserInfoDto user);

        @Operation(summary = "프로필 이미지 변경", description = "프로필 이미지를 변경합니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 이미지 변경 성공"),
        @ApiResponse(responseCode = "409", description = "프로필 이미지 변경 실패")})
        public ResponseEntity<?> modify(@ModelAttribute UserInfoDto userDTO);
}
