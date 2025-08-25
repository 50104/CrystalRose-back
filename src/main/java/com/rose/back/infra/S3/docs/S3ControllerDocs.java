package com.rose.back.infra.S3.docs;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.infra.S3.ImageTempEntity.DomainType;
import com.rose.back.infra.S3.dto.PreSignedUrlResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "S3 Management", description = "AWS S3 파일 관리 API")
public interface S3ControllerDocs {

    @Operation(
        summary = "Pre-signed URL 발급",
        description = """
            파일 업로드를 위한 Pre-signed URL을 발급합니다.
            
            **사용 방법:**
            1. 이 API로 Pre-signed URL을 요청합니다.
            2. 받은 uploadUrl로 직접 S3에 파일을 PUT 요청으로 업로드합니다.
            3. 업로드 완료 후 upload-complete API를 호출합니다.
            
            **지원 파일 형식:**
            - 이미지: JPEG, PNG, GIF, WebP
            - 문서: PDF, DOC, DOCX (DOCUMENT 타입)
            - 기타: 도메인 타입에 따라 제한
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Pre-signed URL 요청 정보",
            content = @Content(
                mediaType = "application/x-www-form-urlencoded",
                examples = {
                    @ExampleObject(
                        name = "게시판 이미지",
                        value = "fileName=image.jpg&contentType=image/jpeg&domainType=BOARD&folderName=boards"
                    ),
                    @ExampleObject(
                        name = "프로필 이미지",
                        value = "fileName=profile.png&contentType=image/png&domainType=USER&folderName=profiles"
                    )
                }
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Pre-signed URL 발급 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PreSignedUrlResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "uploadUrl": "https://bucket.s3.amazonaws.com/path/file.jpg?X-Amz-Signature=...",
                          "accessUrl": "https://dodorose.com/uploads/boards/2025/08/25/uuid.jpg",
                          "key": "uploads/boards/2025/08/25/uuid.jpg"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (파일명 없음, 지원하지 않는 파일 형식 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "지원하지 않는 파일 형식입니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 필요"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류"
        )
    })
    ResponseEntity<PreSignedUrlResponse> getPreSignedUrl(
            @Parameter(
                description = "업로드할 파일명 (확장자 포함)",
                required = true,
                example = "image.jpg"
            )
            @RequestParam String fileName,
            
            @Parameter(
                description = "파일의 MIME 타입",
                required = true,
                example = "image/jpeg"
            )
            @RequestParam String contentType,
            
            @Parameter(
                description = "파일이 사용될 도메인 타입",
                required = true,
                schema = @Schema(implementation = DomainType.class)
            )
            @RequestParam DomainType domainType,
            
            @Parameter(
                description = "파일이 저장될 폴더명 (선택사항)",
                required = false,
                example = "boards"
            )
            @RequestParam(required = false) String folderName,
            
            @AuthenticationPrincipal CustomUserDetails user
    );

    @Operation(
        summary = "파일 업로드 완료 알림",
        description = """
            S3에 파일 업로드 완료 후 서버에 알림을 보냅니다.
            업로드된 파일 정보가 임시 테이블에 저장됩니다.
            
            **호출 시점:**
            - Pre-signed URL로 S3 업로드가 성공한 직후
            - 게시글 저장 시 임시 테이블의 파일들이 정식 테이블로 이동됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "업로드 완료 처리 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "업로드가 완료되었습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<MessageResponse> uploadComplete(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "업로드 완료 정보",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                            {
                              "key": "uploads/boards/2025/08/25/uuid.jpg",
                              "accessUrl": "https://dodorose.com/uploads/boards/2025/08/25/uuid.jpg",
                              "domainType": "BOARD"
                            }
                            """
                    )
                )
            )
            @RequestBody UploadCompleteRequest request,
            
            @AuthenticationPrincipal CustomUserDetails user
    );

    class UploadCompleteRequest {
        private String key;
        private String accessUrl;
        private DomainType domainType;

        public UploadCompleteRequest() {}

        public UploadCompleteRequest(String key, String accessUrl, DomainType domainType) {
            this.key = key;
            this.accessUrl = accessUrl;
            this.domainType = domainType;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        
        public String getAccessUrl() { return accessUrl; }
        public void setAccessUrl(String accessUrl) { this.accessUrl = accessUrl; }

        public DomainType getDomainType() { return domainType; }
        public void setDomainType(DomainType domainType) { this.domainType = domainType; }
    }

    @Operation(
        summary = "파일 삭제",
        description = """
            S3에 업로드된 파일을 삭제합니다.
            
            **주의사항:**
            - 파일 소유자 또는 관리자만 삭제 가능합니다.
            - 삭제된 파일은 복구할 수 없습니다.
            - 게시글에서 사용 중인 파일은 삭제할 수 없습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "파일 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "파일이 삭제되었습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "삭제 권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "파일을 삭제할 권한이 없습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<MessageResponse> deleteFile(
            @Parameter(
                description = "삭제할 파일의 S3 키 (경로 포함)",
                required = true,
                example = "uploads/boards/2025/08/25/uuid.jpg"
            )
            @PathVariable String key,
            
            @AuthenticationPrincipal CustomUserDetails user
    );
}
