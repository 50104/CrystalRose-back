package com.rose.back.domain.board.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentRequestDto{
    @NotBlank(message = "게시물 제목은 필수입니다") 
    @Size(max = 200, message = "게시물 최대 길이 초과 (50자)") 
    private String boardTitle;

    @NotBlank(message = "게시물 내용은 필수입니다") 
    @Size(message = "게시물 내용 최대 길이 초과 (3000자)") 
    private String boardContent;

    @NotBlank(message = "사용자 ID는 필수입니다") 
    private String userId;

    @NotBlank(message = "말머리는 필수입니다")
    private String boardTag;

    private List<MultipartFile> images;

    public ContentRequestDto(String boardTitle, String boardContent, String boardTag, String userId, String image) {
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.boardTag = boardTag;
        this.userId = userId;
    }
}
