package com.rose.back.infra.S3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreSignedUrlResponse {
  
    private final String uploadUrl;
    private final String accessUrl;
    private final String key;
}