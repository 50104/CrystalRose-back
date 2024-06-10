package com.rose.back.board.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.board.entity.Board;
import com.rose.back.board.service.BoardService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BoardController {
    
    private final BoardService boardService;

    @PostMapping("/board")
    public ResponseEntity<?> createBoard(
            @Validated @RequestParam("files") List<MultipartFile> files
    ) throws Exception {
        boardService.addBoard(Board.builder()
                .build(), files);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/board")
    public String getBoard(@RequestParam long id) {
        Board board = boardService.findBoard(id).orElseThrow(RuntimeException::new);
        String imgPath = board.getStoredFileName();
        log.info(imgPath);
        return "<img src=" + imgPath + ">";
    }

}