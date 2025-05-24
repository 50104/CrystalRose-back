package com.rose.back.domain.wiki.controller.docs;

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

import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.global.exception.CommonErrorResponses;

import java.util.Map;

@Tag(name = "Wiki Image", description = "Wiki(도감) 이미지 관련 API입니다.")
public interface WikiImageControllerDocs {

    @Operation(summary = "도감 등록 요청", description = "새로운 도감 등록을 요청합니다. 등록 후 관리자 승인이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도감 등록 요청 성공 (승인 대기 상태로 저장됨)"),
            @ApiResponse(responseCode = "409", description = "도감 등록 요청 실패",
                    content = @Content(schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                            examples = @ExampleObject(name = "Conflict", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "도감 등록 요청에 실패했습니다.",
                                      "path": "/api/v1/wiki/register"
                                    }
                                    """))),
    })
    ResponseEntity<Void> registerWiki(@RequestBody(description = "등록할 도감 정보", required = true,
            content = @Content(schema = @Schema(implementation = WikiRequest.class))) WikiRequest dto);

    @Operation(summary = "도감 이미지 업로드", description = "도감에 사용될 이미지를 업로드합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            @ApiResponse(responseCode = "409", description = "이미지 업로드 실패",
                    content = @Content(schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                            examples = @ExampleObject(name = "Conflict", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "이미지 업로드에 실패했습니다.",
                                      "path": "/api/v1/wiki/image/upload"
                                    }
                                    """))),
    })
    ResponseEntity<Map<String, Object>> upload (
            @RequestBody(description = "업로드할 이미지 파일", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            MultipartFile file
    );
}