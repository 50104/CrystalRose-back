package com.rose.back.common.util;

import org.springframework.data.domain.Page;

import com.rose.back.common.dto.PageResponse;

public class PageUtil {
    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber() + 1,
            page.getTotalPages(),
            page.getTotalElements(),
            page.isFirst(),
            page.isLast()
        );
    }
}