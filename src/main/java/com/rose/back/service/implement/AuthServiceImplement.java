package com.rose.back.service.implement;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rose.back.dto.request.auth.IdCheckRequestDto;
import com.rose.back.dto.response.ResponseDto;
import com.rose.back.dto.response.auth.IdCheckResponseDto;
import com.rose.back.repository.UserRepository;
import com.rose.back.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {
    
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<? super IdCheckResponseDto> userIdCheck(IdCheckRequestDto dto) {

        try {

            String userId = dto.getUserId();
            boolean isExistId = userRepository.existsByUserId(userId);
            if (isExistId) return IdCheckResponseDto.duplicateId();
            
        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return IdCheckResponseDto.success();
    }
    
}
