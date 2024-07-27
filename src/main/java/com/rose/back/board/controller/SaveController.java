package com.rose.back.board.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.board.dto.SaveDto;
import com.rose.back.board.service.ContentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class SaveController {

    private final ContentService contentService;

    @PostMapping("/save")
    public String saveLogic(SaveDto saveDTO) {
        log.info("[POST][/board/save] - 게시글 저장 컨트롤러{}", saveDTO);
        contentService.saveContent(saveDTO);
        return "redirect:/";
    }
}
