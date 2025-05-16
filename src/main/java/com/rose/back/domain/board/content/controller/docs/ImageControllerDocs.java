package com.rose.back.domain.board.content.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.exception.ImageUploadErrorResponses;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.util.Map;

@Tag(name = "Image", description = "이미지 업로드 관련 API입니다.")
public interface ImageControllerDocs {

    @Operation(summary = "이미지 업로드", description = "이미지를 업로드하고 S3 URL을 반환합니다.")
    @CommonErrorResponses
    @ImageUploadErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "이미지 업로드 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미지 업로드에 실패했습니다.",
                      "path": "/board/image/upload",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, Object>> imageUpload(@RequestParam("file") MultipartFile file) throws Exception;
}