package com.rose.back.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IdCheckRequest {
    
    @NotBlank
    private String userId;
}
