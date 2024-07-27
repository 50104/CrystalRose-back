package com.rose.back.board.service;

import org.springframework.stereotype.Service;

import com.rose.back.board.dto.SaveDto;
import com.rose.back.board.entity.ContentEntity;
import com.rose.back.board.repository.ContentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    public void saveContent(SaveDto saveDto) {
        String title = saveDto.getTitle();
        String content = saveDto.getContent();
        ContentEntity content1 = new ContentEntity();
        content1.setBoardTitle(title);
        content1.setBoardContent(content);
        contentRepository.save(content1);
        return;
    }
}