package com.rose.back.board.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.board.service.ContentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/content/{boardNo}")
    public Map<String, Object> contentPage(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/board/content/{}] - 콘텐츠 컨트롤러", boardNo);
        Map<String, Object> response = new HashMap<>();
        response.put("Content", contentService.selectOneContent(boardNo));
        return response;
    }
}
