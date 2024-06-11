package com.rose.back.user.service.implement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rose.back.user.repository.CertificationRepository;
import com.rose.back.user.repository.UserRepository;
import com.rose.back.user.common.CertificationNumber;
import com.rose.back.user.dto.request.auth.CheckCertificationRequestDto;
import com.rose.back.user.dto.request.auth.EmailCertificationRequestDto;
import com.rose.back.user.dto.request.auth.IdCheckRequestDto;
import com.rose.back.user.dto.request.auth.SignInRequestDto;
import com.rose.back.user.dto.request.auth.SignUpRequestDto;
import com.rose.back.user.dto.response.ResponseDto;
import com.rose.back.user.dto.response.auth.CheckCertificationResponseDto;
import com.rose.back.user.dto.response.auth.EmailCertificationResponseDto;
import com.rose.back.user.dto.response.auth.IdCheckResponseDto;
import com.rose.back.user.dto.response.auth.SignInResponseDto;
import com.rose.back.user.dto.response.auth.SignUpResponseDto;
import com.rose.back.user.entity.CertificationEntity;
import com.rose.back.user.entity.UserEntity;
import com.rose.back.user.provider.EmailProvider;
import com.rose.back.user.provider.JwtProvider;
import com.rose.back.user.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {
    
    // final을 붙여서 외부에서 제어의 역전을 통해 의존성 주입
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;

    private final JwtProvider jwtProvider;

    private final EmailProvider emailProvider;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    // 위는 final을 붙여서 직접 의존성을 주입하지않고 @Service외부를 통해 주입하고있는데 어떤걸 주입할지 직접 선택함

    // ID 중복 확인
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

    // 이메일 인증
    @Override
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto) {
        
        try {

            String userId = dto.getUserId();
            String userEmail = dto.getUserEmail();

            // 이메일 여부 확인
            boolean isExistId = userRepository.existsByUserId(userId);
            if (isExistId) return EmailCertificationResponseDto.duplicateId();
            
            // 번호 생성
            String certificationNumber = CertificationNumber.getCertificationNumber(); // 임의의 4자리수 받아오기

            // 메일 전송
            boolean isSuccessed = emailProvider.sendCertificationMail(userEmail, certificationNumber);
            if (!isSuccessed) return EmailCertificationResponseDto.mailSendFail();

            // 전송 결과 저장
            CertificationEntity certificationEntity = new CertificationEntity(null, userId, userEmail, certificationNumber);
            certificationRepository.save(certificationEntity);

        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return EmailCertificationResponseDto.success();
    }

    // 인증 확인
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

    // 회원가입
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

    // 로그인
    @Override
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {
        
        String token = null;

        try {

            String userId = dto.getUserId();
            UserEntity userEntity = userRepository.findByUserId(userId);
            if(userEntity == null) return SignInResponseDto.signInFail();

            String userPwd = dto.getUserPwd();
            String encodedPwd = userEntity.getUserPwd();
            boolean isMatched = passwordEncoder.matches(userPwd, encodedPwd);
            if(!isMatched) return SignInResponseDto.signInFail();

            token = jwtProvider.create(userId);
            
        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return SignInResponseDto.success(token);
    }
}
