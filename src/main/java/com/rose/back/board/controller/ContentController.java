package com.rose.back.board.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class ContentController {
    
    @GetMapping("/content")
    public String contentPage() {
        log.info("[GET][/board/content] - 콘텐츠 컨트롤러");
        return "content";
    }
}
