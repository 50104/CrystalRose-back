package com.rose.back.domain.chat.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.domain.chat.dto.ChatRoomInfoDto;
import com.rose.back.domain.chat.dto.ChatRoomListResDto;
import com.rose.back.domain.chat.dto.MyChatListResDto;
import com.rose.back.domain.chat.dto.RoomIdResponse;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Chat", description = "채팅방 및 메시지 관련 API입니다.")
public interface ChatControllerDocs {

    @Operation(summary = "그룹 채팅방 개설", description = "새로운 그룹 채팅방을 생성합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 생성 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "채팅방 생성 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "채팅방 생성에 실패했습니다.",
                      "path": "/api/v1/chat/rooms"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> createGroupRoom(@RequestParam("roomName") String roomName);

    @Operation(summary = "그룹 채팅방 목록 조회", description = "모든 그룹 채팅방 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "채팅방 목록 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "채팅방 목록을 조회할 수 없습니다.",
                      "path": "/api/v1/chat/rooms"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<List<ChatRoomListResDto>> getGroupChatRooms();

    @Operation(summary = "그룹 채팅방 참여", description = "특정 그룹 채팅방에 참여합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 참여 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "채팅방 참여 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "채팅방 참여에 실패했습니다.",
                      "path": "/api/v1/chat/join/{roomId}"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> joinGroupChatRoom(@PathVariable("roomId") Long roomId);

    @Operation(summary = "이전 메시지 조회", description = "특정 채팅방의 이전 메시지 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이전 메시지 조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "이전 메시지 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이전 메시지 조회에 실패했습니다.",
                      "path": "/api/v1/chat/history/{roomId}"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<List<ChatMessageReqDto>> getChatHistory(@PathVariable("roomId") Long roomId, @RequestParam(required = false) LocalDateTime cursor);

    @Operation(summary = "채팅 메시지 읽음 처리", description = "특정 채팅방의 메시지를 읽음 처리합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "메시지 읽음 처리 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "메시지 읽음 처리 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "메시지 읽음 처리에 실패했습니다.",
                      "path": "/api/v1/chat/read/{roomId}"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> messageRead(@PathVariable("roomId") Long roomId);

    @Operation(summary = "내 채팅방 목록 조회", description = "내가 참여 중인 채팅방 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 채팅방 목록 조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "내 채팅방 목록 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "내 채팅방 목록을 조회할 수 없습니다.",
                      "path": "/api/v1/chat/my-rooms"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<List<MyChatListResDto>> getMyChatRooms();

    @Operation(summary = "채팅방 나가기", description = "특정 그룹 채팅방에서 나갑니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 나가기 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "채팅방 나가기 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "채팅방 나가기에 실패했습니다.",
                      "path": "/api/v1/chat/leave/{roomId}"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<?> leaveGroupChatRoom(@PathVariable("roomId") Long roomId);

    @Operation(summary = "개인 채팅방 개설 또는 조회", description = "상대방과의 개인 채팅방을 개설하거나 기존 roomId를 반환합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "개인 채팅방 개설/조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "개인 채팅방 개설/조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "개인 채팅방 개설 또는 조회에 실패했습니다.",
                      "path": "/api/v1/chat/private-room"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<RoomIdResponse> getOrCreatePrivateRoom(@RequestParam("otherMemberId") Long otherMemberId);

    @Operation(summary = "채팅방 정보 조회", description = "특정 채팅방의 정보를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 정보 조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "채팅방 정보 조회 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "채팅방 정보 조회에 실패했습니다.",
                      "path": "/api/v1/chat/room/{roomId}/info"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<ChatRoomInfoDto> getChatRoomInfo(@PathVariable Long roomId);
}