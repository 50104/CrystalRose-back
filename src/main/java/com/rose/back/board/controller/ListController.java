package com.rose.back.board.controller;

import org.springframework.web.bind.annotation.RestController;

import com.rose.back.board.service.ContentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class ListController {

    private final ContentService contentService;

    @GetMapping("/list")
    public Map<String, Object> listPage() {
        log.info("[GET][/board/list] - 리스트 컨트롤러");
        Map<String, Object> response = new HashMap<>();
        response.put("ContentList", contentService.selectContent());
        return response;
    }
}
