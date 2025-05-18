package com.rose.back.domain.board.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContentRequestDto{
    @NotBlank(message = "[NotBlank] 게시물 제목") 
    @Size(max = 200, message = "게시물 최대 길이 초과 (50자)") 
    private String boardTitle;

    @NotBlank(message = "[NotBlank] 게시물 내용") 
    @Size(message = "게시물 내용 최대 길이 초과 (3000자)") 
    private String boardContent;

    @NotBlank(message = "[NotBlank] 사용자 ID ") 
    private String userId;

    private List<MultipartFile> images;
}
