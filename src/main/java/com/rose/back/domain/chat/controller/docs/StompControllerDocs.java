package com.rose.back.domain.chat.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@Tag(name = "Stomp", description = "채팅(Stomp) 관련 API입니다.")
public interface StompControllerDocs {

    @Operation(summary = "채팅 메시지 전송", description = "특정 채팅방(roomId)으로 메시지를 전송합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "메시지 전송 실패(비즈니스 로직 충돌)",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "메시지 전송에 실패했습니다.",
                      "path": "/room/{roomId}/send"
                    }
                    """
                )
            )
        )
    })
    @MessageMapping("/{roomId}")
    void sendMessage(@DestinationVariable("roomId") Long roomId, ChatMessageReqDto chatMessageReqDto);
}