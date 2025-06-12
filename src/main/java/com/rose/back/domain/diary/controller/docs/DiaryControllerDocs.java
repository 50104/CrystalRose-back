package com.rose.back.domain.diary.controller.docs;

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

import java.util.List;
import java.util.Map;

@Tag(name = "Diary", description = "다이어리 관련 API")
public interface DiaryControllerDocs {

    @Operation(summary = "성장 기록 등록", description = "지정된 장미에 대해 성장 기록을 등록합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성장 기록 등록 성공"),
        @ApiResponse(responseCode = "409", description = "성장 기록 등록 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "InternalError", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "등록 실패",
                      "path": "/api/diaries/{roseId}"
                    }
                """)))
    })
    ResponseEntity<?> addDiary(
        @RequestBody(description = "등록할 다이어리 정보", required = true,
            content = @Content(schema = @Schema(implementation = Object.class))) Object request
    );

    @Operation(summary = "다이어리 이미지 업로드", description = "다이어리용 이미지를 업로드하고 URL을 반환합니다.")
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
                      "path": "/api/diaries/image/upload"
                    }
                """)))
    })
    ResponseEntity<Map<String, Object>> uploadDiaryImage(
        @RequestBody(description = "업로드할 이미지", required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(type = "string", format = "binary"))) MultipartFile file
    );

    @Operation(summary = "내 성장기록 조회", description = "내 모든 장미의 성장 기록을 시간 역순으로 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성장 기록 조회 성공"),
        @ApiResponse(responseCode = "409", description = "성장 기록 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "InternalError", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "성장기록 조회 실패",
                      "path": "/api/diaries/timeline"
                    }
                """)))
    })
    ResponseEntity<List<Object>> getMyTimeline();
}
