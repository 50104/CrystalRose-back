package com.rose.back.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IdCheckRequestDto {
    
    @NotBlank
    private String userId;
}
