package com.rose.back.service.implement;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rose.back.common.CertificationNumber;
import com.rose.back.dto.request.auth.CheckCertificationRequestDto;
import com.rose.back.dto.request.auth.EmailCertificationRequestDto;
import com.rose.back.dto.request.auth.IdCheckRequestDto;
import com.rose.back.dto.response.ResponseDto;
import com.rose.back.dto.response.auth.CheckCertificationResponseDto;
import com.rose.back.dto.response.auth.EmailCertificationResponseDto;
import com.rose.back.dto.response.auth.IdCheckResponseDto;
import com.rose.back.entity.CertificationEntity;
import com.rose.back.provider.EmailProvider;
import com.rose.back.repository.CertificationRepository;
import com.rose.back.repository.UserRepository;
import com.rose.back.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {
    
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;

    private final EmailProvider emailProvider;

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

    @Override
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto) {
        
        try {

            String userId = dto.getUserId();
            String userEmail = dto.getUserEmail();

            // 이메일 여부 확인
            boolean isExistId = userRepository.existsByUserId(userId);
            if (isExistId) return EmailCertificationResponseDto.duplicateId();
            
            // 번호 생성
            String certificationNumber = CertificationNumber.getCertificationNumber();

            // 메일 전송
            boolean isSuccessed = emailProvider.sendCertificationMail(userEmail, certificationNumber);
            if (!isSuccessed) return EmailCertificationResponseDto.mailSendFail();

            // 전송 결과 저장
            CertificationEntity certificationEntity = new CertificationEntity(userId, userEmail, certificationNumber);
            certificationRepository.save(certificationEntity);

        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return EmailCertificationResponseDto.success();
    }

    @Override
    public ResponseEntity<? super CheckCertificationResponseDto> checkCertification(CheckCertificationRequestDto dto) {
        
        try {
            
            String userId = dto.getUserId();
            String userEmail = dto.getUserEmail();
            String certificationNumber = dto.getCertificationNumber();

            CertificationEntity certificationEntity = certificationRepository.findByUserId(userId);
            if (certificationEntity == null) return CheckCertificationResponseDto.certificationFail();

            boolean isMatched = certificationEntity.getUserEmail().equals(userEmail) && certificationEntity.getCertificationNo().equals(certificationNumber);
            if (!isMatched) return CheckCertificationResponseDto.certificationFail();

        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return CheckCertificationResponseDto.success();
    }
}
