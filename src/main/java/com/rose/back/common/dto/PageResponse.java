package com.rose.back.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
  
    private List<T> content;
    private int currentPage;
    private int totalPage;
    private long totalElements;
    private boolean isFirst;
    private boolean isLast;
}