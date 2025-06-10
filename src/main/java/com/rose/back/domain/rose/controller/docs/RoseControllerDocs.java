package com.rose.back.domain.rose.controller.docs;

import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Rose", description = "장미 등록 및 이미지 업로드 API")
public interface RoseControllerDocs {

    @Operation(summary = "내 장미 등록", description = "도감에서 선택한 품종으로 내 장미를 등록합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장미 등록 성공"),
        @ApiResponse(responseCode = "409", description = "장미 등록 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "InternalError", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "내 장미 등록 실패",
                      "path": "/api/roses/mine"
                    }
                """)))
    })
    ResponseEntity<?> registerRose(
        @RequestBody(description = "등록할 장미 정보", required = true,
            content = @Content(schema = @Schema(implementation = Object.class))) Object dto
    );

    @Operation(summary = "장미 이미지 업로드", description = "S3에 장미 이미지를 업로드하고 URL을 반환합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
        @ApiResponse(responseCode = "409", description = "이미지 업로드 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "UploadFail", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미지 업로드 실패",
                      "path": "/api/roses/image/upload"
                    }
                """)))
    })
    ResponseEntity<Map<String, Object>> uploadImage(
        @RequestBody(description = "업로드할 이미지", required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(type = "string", format = "binary"))) MultipartFile file
    );
}
