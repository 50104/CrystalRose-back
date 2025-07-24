package com.rose.back.global.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getAllErrors()
                      .stream()
                      .limit(5)
                      .map(err -> err.getDefaultMessage())
                      .collect(Collectors.joining("; "));

        ErrorResponse err = ErrorResponse.builder()
            .timestamp(now())
            .status(HttpStatus.BAD_REQUEST.value())
            .code("BAD_REQUEST")
            .message(msg)
            .path(req.getRequestURI())
            .build();

        log.warn("Validation failed @ {}: {}", req.getRequestURI(), msg);
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        String uri = req.getRequestURI();

        if (uri.contains("/xhr") || uri.contains("/info") || uri.contains("/websocket")) {
            log.debug("Skip exception handling for SockJS fallback: {}", uri);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        ErrorResponse err = ErrorResponse.builder()
            .timestamp(now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .code("INTERNAL_ERROR")
            .message("서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
            .path(uri)
            .build();

        log.error("Unhandled error @ {}: {}", uri, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResponse> handleUploadError(Exception ex, HttpServletRequest req) {
        ErrorResponse err = ErrorResponse.builder()
            .timestamp(now())
            .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
            .code("UPLOAD_ERROR")
            .message("파일 용량이 허용 한도를 초과했습니다.")
            .path(req.getRequestURI())
            .build();

        log.error("Upload error @ {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String message = "잘못된 JSON 형식입니다.";
        ErrorResponse err = ErrorResponse.builder()
            .timestamp(now())
            .status(HttpStatus.BAD_REQUEST.value())
            .code("JSON_PARSE_ERROR")
            .message(message)
            .path(req.getRequestURI())
            .build();

        log.warn("JSON parse error @ {}: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, HttpServletRequest req) {
        ErrorResponse err = ErrorResponse.builder()
            .timestamp(now())
            .status(HttpStatus.CONFLICT.value())
            .code("CONFLICT")
            .message(ex.getMessage())
            .path(req.getRequestURI())
            .build();

        log.warn("IllegalStateException @ {}: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest req) {
        ErrorResponse err = ErrorResponse.builder()
            .timestamp(now())
            .status(ex.getStatusCode().value())
            .code("RESPONSE_STATUS_ERROR")
            .message(ex.getReason() != null ? ex.getReason() : "요청 처리 중 오류가 발생했습니다.")
            .path(req.getRequestURI())
            .build();

        log.warn("ResponseStatusException @ {}: {}", req.getRequestURI(), ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(err);
    }
}
