package com.rose.back.service.implement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rose.back.common.CertificationNumber;
import com.rose.back.dto.request.auth.CheckCertificationRequestDto;
import com.rose.back.dto.request.auth.EmailCertificationRequestDto;
import com.rose.back.dto.request.auth.IdCheckRequestDto;
import com.rose.back.dto.request.auth.SignUpRequestDto;
import com.rose.back.dto.response.ResponseDto;
import com.rose.back.dto.response.auth.CheckCertificationResponseDto;
import com.rose.back.dto.response.auth.EmailCertificationResponseDto;
import com.rose.back.dto.response.auth.IdCheckResponseDto;
import com.rose.back.dto.response.auth.SignUpResponseDto;
import com.rose.back.entity.CertificationEntity;
import com.rose.back.entity.UserEntity;
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

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    // 위는 final을 붙여서 직접 의존성을 주입하지않고 @Service외부를 통해 주입하고있는데 어떤걸 주입할지 직접 선택함

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

            boolean isMatched = certificationEntity.getUserEmail().equals(userEmail) && certificationEntity.getCertificationNumber().equals(certificationNumber);
            if (!isMatched) return CheckCertificationResponseDto.certificationFail();

        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return CheckCertificationResponseDto.success();
    }

    @Override
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {
        
        try {

            String userId = dto.getUserId();
            boolean isExistId = userRepository.existsByUserId(userId);
            if(isExistId) return SignUpResponseDto.duplicateId();

            String userEmail = dto.getUserEmail();
            String certificationNumber = dto.getCertificationNumber();
            CertificationEntity certificationEntity = certificationRepository.findByUserId(userId);
            boolean isMatched = 
                certificationEntity.getUserEmail().equals(userEmail) && 
                certificationEntity.getCertificationNumber().equals(certificationNumber);
            if(!isMatched) return SignUpResponseDto.certificationFail();

            String userPwd = dto.getUserPwd();
            String encodedPassword = passwordEncoder.encode(userPwd);
            dto.setUserPwd(encodedPassword);

            UserEntity userEntity = new UserEntity(dto);
            userRepository.save(userEntity);

            certificationRepository.deleteByUserId(userId);
            // certificationRepository.delete(certificationEntity);
            
        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return SignUpResponseDto.success();
    }
}
