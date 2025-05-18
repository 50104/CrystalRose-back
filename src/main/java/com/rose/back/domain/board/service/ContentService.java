package com.rose.back.domain.board.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.entity.ImageEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.ImageRepository;

import java.util.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public Long saveContent(ContentRequestDto req) {
        ContentEntity content = new ContentEntity();
        content.setBoardTitle(req.getBoardTitle());
        content.setBoardContent(req.getBoardContent());
        content.setUserId(req.getUserId());
        ContentEntity savedContent = contentRepository.save(content);

        Document doc = Jsoup.parse(req.getBoardContent());
        Elements images = doc.select("img");

        for (Element img : images) {
            String imageUrl = img.attr("src");
            imageRepository.save(ImageEntity.builder()
                .fileUrl(imageUrl)
                .storedFileName(imageUrl.substring(imageUrl.lastIndexOf("/") + 1))
                .content(savedContent)
                .build());
        }
        return savedContent.getBoardNo();
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