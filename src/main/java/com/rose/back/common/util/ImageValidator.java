package com.rose.back.common.util;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class ImageValidator {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        String name = file.getOriginalFilename().toLowerCase();
        if (ALLOWED_EXTENSIONS.stream().noneMatch(name::endsWith)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하만 허용됩니다.");
        }
    }
}
