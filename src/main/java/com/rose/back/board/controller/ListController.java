package com.rose.back.board.controller;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class ListController {
    
    @GetMapping("/list")
    public String listPage() {
        log.info("[GET][/board/list] - 리스트 컨트롤러");
        return "list";
    }
}
