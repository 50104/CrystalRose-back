package com.rose.back.board.content.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.board.content.dto.ContentRequestDto;
import com.rose.back.board.content.entity.Board;
import com.rose.back.board.content.entity.ContentEntity;
import com.rose.back.board.content.repository.ContentRepository;
import java.util.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    @Transactional
    public void saveContent(ContentRequestDto req) {
        ContentEntity content = new ContentEntity();
        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        content.setUserId(req.getUserId());
        contentRepository.save(content);
    }

    public List<ContentEntity> selectContent() {
        return contentRepository.findAll();
    }

    public ContentEntity selectOneContent(Long boardNo) {
        return contentRepository.findByBoardNo(boardNo)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 게시글: " + boardNo));
    }

    @Transactional
    public void deleteOneContent(Long boardNo) {
        contentRepository.deleteByBoardNo(boardNo);
    }

    public void updateOneContent(ContentRequestDto req, Long boardNo) {
        ContentEntity content = new ContentEntity();
        content.setBoardNo(boardNo);
        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        content.setUserId(req.getUserId());
        contentRepository.save(content);
    }
}