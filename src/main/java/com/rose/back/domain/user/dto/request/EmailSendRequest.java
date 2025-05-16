package com.rose.back.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailSendRequest {
    
    @NotBlank
    private String userId;

    @NotBlank
    @Email
    private String userEmail;
}
