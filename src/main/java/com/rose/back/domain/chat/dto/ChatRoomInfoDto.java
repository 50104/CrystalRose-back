package com.rose.back.domain.chat.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatRoomInfoDto {
    private Long roomId;
    private String roomName;
    private String isGroupChat;
    private List<ParticipantDto> participants;

    @Builder
    @Getter
    public static class ParticipantDto {
        private String userId;
        private String userNick;
        private String userProfileImg;
    }
}
