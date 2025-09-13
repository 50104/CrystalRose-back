package com.rose.back.domain.wiki.controller.docs;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.wiki.dto.WikiModificationDetailDto;
import com.rose.back.domain.wiki.dto.WikiModificationListDto;
import com.rose.back.domain.wiki.dto.WikiModificationRequestDto;
import com.rose.back.domain.wiki.dto.WikiModificationResubmitDto;
import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.domain.wiki.dto.WikiResponse;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Wiki", description = "Wiki(도감) 관련 API입니다.")
public interface WikiControllerDocs {

    @Operation(summary = "도감 등록 요청", description = "새로운 도감 등록을 요청합니다. 등록 후 관리자 승인이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도감 등록 요청 성공 (승인 대기 상태로 저장됨)"),
            @ApiResponse(responseCode = "409", description = "도감 등록 요청 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
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
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
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

    @Operation(summary = "도감 수정 요청", description = "도감을 수정하고 관리자에게 승인을 요청합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도감 수정 요청 성공"),
            @ApiResponse(responseCode = "409", description = "도감 수정 요청 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Conflict", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "도감 수정 요청에 실패했습니다.",
                                      "path": "/api/v1/wiki/modify/{id}"
                                    }
                                    """))),
    })
    ResponseEntity<MessageResponse> submitModificationRequest(@PathVariable("id") Long id, @RequestBody @Valid WikiRequest dto);

    @Operation(summary = "승인된 도감 목록 조회", description = "관리자가 승인한 도감 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "승인된 도감 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = WikiResponse.class)))
            ),
            @ApiResponse(responseCode = "409", description = "승인된 도감 목록 조회 실패",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Conflict", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "승인된 도감 목록 조회에 실패했습니다.",
                                      "path": "/api/v1/wiki/list"
                                    }
                                    """))),
    })
    ResponseEntity<List<WikiResponse>> getWikiList();

    @Operation(summary = "거절된 도감 수정 요청 조회", description = "거절된 도감 수정 요청의 정보를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거절된 도감 수정 요청 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = WikiModificationRequestDto.class)))),
            @ApiResponse(responseCode = "409", description = "거절된 도감 수정 요청 정보 조회 실패 - 도감이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 도감을 찾을 수 없습니다.",
                                      "path": "/api/v1/wiki/modification/rejected"
                                    }
                                    """))),
    })
    ResponseEntity<WikiResponse> getWikiDetail(@RequestBody(description = "조회할 도감 ID", required = true,
            content = @Content(schema = @Schema(type = "integer"))) Long id);

    @Operation(summary = "사용자 도감 수정 요청 목록 조회", description = "사용자가 제출한 도감 수정 요청 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 도감 수정 요청 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = WikiModificationListDto.class)))),
            @ApiResponse(responseCode = "409", description = "사용자 도감 수정 요청 목록 조회 실패 - 사용자가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 사용자를 찾을 수 없습니다.",
                                      "path": "/api/v1/wiki/user/modification"
                                    }
                                    """))),
    })
    ResponseEntity<List<WikiModificationListDto>> getUserModifications(@AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "거절된 도감 수정 요청 조회", description = "거절된 도감 수정 요청의 상세 정보를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거절된 도감 수정 요청 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WikiModificationResubmitDto.class))),
            @ApiResponse(responseCode = "409", description = "거절된 도감 수정 요청 정보 조회 실패 - 요청이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 수정 요청을 찾을 수 없습니다.",
                                      "path": "/api/v1/wiki/user/modification/{id}"
                                    }
                                    """))),
    })
    ResponseEntity<WikiModificationResubmitDto> getRejectedModification(@PathVariable("id") Long id,
                                                                        @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "도감 보완 제출 요청", description = "거절된 도감 수정 요청에 대해 보완 제출을 요청합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도감 보완 제출 요청 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "409", description = "도감 보완 제출 요청 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Conflict", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "도감 보완 제출 요청에 실패했습니다.",
                                      "path": "/api/v1/wiki/user/modification/{id}/resubmit"
                                    }
                                    """))),
    })
    ResponseEntity<MessageResponse> resubmit(@PathVariable("id") Long id,
                                              @RequestBody(description = "보완 제출할 도감 수정 요청 정보", required = true,
                                                      content = @Content(schema = @Schema(implementation = WikiModificationResubmitDto.class))) WikiModificationResubmitDto dto,
                                              @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "사용자 도감 목록 조회", description = "사용자가 신청한 도감 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 도감 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WikiResponse.class))),
            @ApiResponse(responseCode = "409", description = "사용자 도감 목록 조회 실패 - 사용자가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 사용자를 찾을 수 없습니다.",
                                      "path": "/api/v1/wiki/user/list"
                                    }
                                    """))),
    })
    public ResponseEntity<Page<WikiResponse>> getMyWikis(@AuthenticationPrincipal CustomUserDetails principal, @RequestParam(value = "status", required = false) List<String> statusStrings, Pageable pageable);

    @Operation(summary = "거절된 도감 목록 조회", description = "사용자가 거절된 도감 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거절된 도감 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WikiResponse.class))),
            @ApiResponse(responseCode = "409", description = "거절된 도감 목록 조회 실패 - 사용자가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 사용자를 찾을 수 없습니다.",
                                      "path": "/api/v1/wiki/user/rejected"
                                    }
                                    """))),
    })
    public ResponseEntity<Page<WikiResponse>> getMyRejectedWikis(@AuthenticationPrincipal CustomUserDetails principal, Pageable pageable);

    @Operation(summary = "사용자 도감 수정 요청 상세 조회", description = "사용자가 요청한 도감 수정 요청의 상세 정보를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 도감 수정 요청 상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WikiModificationDetailDto.class))),
            @ApiResponse(responseCode = "409", description = "사용자 도감 수정 요청 상세 조회 실패 - 도감이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 도감을 찾을 수 없습니다.",
                                      "path": "/api/v1/wiki/user/{id}"
                                    }
                                    """))),
    })
    public ResponseEntity<WikiModificationDetailDto> getUserModificationDetail(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "도감 제출 취소", description = "사용자가 제출한 도감을 취소합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도감 제출 취소 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "409", description = "도감 제출 취소 실패 - 도감이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 도감이 존재하지 않습니다.",
                                      "path": "/api/v1/wiki/user/{id}"
                                    }
                                    """))),
    })
    ResponseEntity<MessageResponse> cancelMyWiki(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "사용자 도감 수정 요청 취소", description = "사용자가 요청한 도감 수정 요청을 취소합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 도감 수정 요청 취소 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "409", description = "사용자 도감 수정 요청 취소 실패 - 도감이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "status": 409,
                                      "error": "CONFLICT",
                                      "message": "해당 도감이 존재하지 않습니다.",
                                      "path": "/api/v1/wiki/user/{id}"
                                    }
                                    """))),
    })
    public ResponseEntity<MessageResponse> cancelUserModification(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "거절된 도감 보완 재제출", description = "거절된 도감(위키)을 수정하여 다시 심사 요청(PENDING) 상태로 재제출합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거절된 도감 보완 재제출 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)) ),
            @ApiResponse(responseCode = "409", description = "거절된 도감 보완 재제출 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Conflict", value = """
                                    {
                                      \"status\": 409,
                                      \"error\": \"CONFLICT\",
                                      \"message\": \"거절된 도감 보완 재제출에 실패했습니다.\",
                                      \"path\": "/api/v1/wiki/user/{id}/resubmit"
                                    }
                                    """)))
    })
    ResponseEntity<MessageResponse> resubmitRejectedWiki(@PathVariable("id") Long id,
                                                          @RequestBody(description = "보완 제출할 도감 수정 내용", required = true, content = @Content(schema = @Schema(implementation = WikiRequest.class))) WikiRequest dto,
                                                          @AuthenticationPrincipal CustomUserDetails user);
}