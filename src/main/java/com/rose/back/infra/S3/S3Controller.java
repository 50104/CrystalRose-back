package com.rose.back.infra.S3;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.infra.S3.docs.S3ControllerDocs;
import com.rose.back.infra.S3.dto.PreSignedUrlResponse;
import com.rose.back.infra.S3.ImageTempEntity.DomainType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller implements S3ControllerDocs {

    private final S3PresignedService s3PresignedService;

    @Override
    @PostMapping("/presigned-url")
    public ResponseEntity<PreSignedUrlResponse> getPreSignedUrl(
            @RequestParam(name = "fileName") String fileName,
            @RequestParam(name = "contentType") String contentType,
            @RequestParam(name = "domainType") DomainType domainType,
            @RequestParam(name = "folderName", required = false) String folderName,
            @AuthenticationPrincipal CustomUserDetails user) {
        log.info("Pre-signed URL 요청: fileName={}, contentType={}, domainType={}, folderName={}, user={}", fileName, contentType, domainType, folderName, user.getUsername());

        try {
            if (!s3PresignedService.isValidFileType(contentType, domainType)) {
                log.warn("지원하지 않는 파일 형식: {} for domain: {}", contentType, domainType);
                return ResponseEntity.badRequest().build();
            }
            if (fileName == null || fileName.trim().isEmpty()) {
                log.warn("파일명이 비어있음");
                return ResponseEntity.badRequest().build();
            }
            String targetFolder = s3PresignedService.generateFolderPath(domainType, folderName, user.getUsername());
            PreSignedUrlResponse response = s3PresignedService.generatePreSignedUrl(fileName, contentType, targetFolder);
            
            log.info("Pre-signed URL 발급 성공: key={}", response.getKey());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Pre-signed URL 발급 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    @PostMapping("/upload-complete")
    public ResponseEntity<MessageResponse> uploadComplete(@RequestBody S3ControllerDocs.UploadCompleteRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("업로드 완료 알림: key={}, url={}, domainType={}, user={}", request.getKey(), request.getAccessUrl(), request.getDomainType(), user.getUsername());

        s3PresignedService.saveToTempTable(request.getAccessUrl(), request.getKey(), request.getDomainType(), user.getUsername());
        return ResponseEntity.ok(new MessageResponse("업로드가 완료되었습니다."));
    }

    @Override
    @DeleteMapping("/{key:.*}")
    public ResponseEntity<MessageResponse> deleteFile(@PathVariable("key") String key, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("파일 삭제 요청: key={}, user={}", key, user.getUsername());

        try {
            if (!s3PresignedService.hasDeletePermission(key, user)) {
                return ResponseEntity.status(403).body(new MessageResponse("파일을 삭제할 권한이 없습니다."));
            }
            s3PresignedService.deleteFile(key);
            log.info("파일 삭제 완료: key={}", key);
            return ResponseEntity.ok(new MessageResponse("파일이 삭제되었습니다."));
            
        } catch (Exception e) {
            log.error("파일 삭제 실패: key={}", key, e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("파일 삭제에 실패했습니다."));
        }
    }
}
