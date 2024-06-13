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

            CertificationEntity certificationEntity = certificationRepository.findByUserId(userId); // user_id로 찾아옴
            if (certificationEntity == null) return CheckCertificationResponseDto.certificationFail(); // 없으면 실패

            boolean isMatched = certificationEntity.getUserEmail().equals(userEmail) && certificationEntity.getCertificationNumber().equals(certificationNumber); // 이메일과 인증번호가 일치하는지 확인
            if (!isMatched) return CheckCertificationResponseDto.certificationFail(); // 일치하지않으면 실패

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
            boolean isExistId = userRepository.existsByUserId(userId); // id가 존재하는지 확인
            if(isExistId) return SignUpResponseDto.duplicateId(); // 존재하면 실패

            String userEmail = dto.getUserEmail();
            String certificationNumber = dto.getCertificationNumber();
            CertificationEntity certificationEntity = certificationRepository.findByUserId(userId); // user_id로 찾아옴
            boolean isMatched = 
                certificationEntity.getUserEmail().equals(userEmail) && 
                certificationEntity.getCertificationNumber().equals(certificationNumber); // 이메일과 인증번호가 일치하는지 확인
            if(!isMatched) return SignUpResponseDto.certificationFail(); // 일치하지않으면 실패

            String userPwd = dto.getUserPwd();
            String encodedPassword = passwordEncoder.encode(userPwd); // 비밀번호 암호화
            dto.setUserPwd(encodedPassword); // 암호화된 비밀번호로 변경

            UserEntity userEntity = new UserEntity(dto); // 회원가입을 위한 생성자
            userRepository.save(userEntity); // 저장

            certificationRepository.deleteByUserId(userId); // user_id로 삭제
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
            String userRole = "";
            UserEntity userEntity = userRepository.findByUserId(userId); // user_id로 찾아옴
            if(userEntity == null) return SignInResponseDto.signInFail(); // 없으면 실패

            String userPwd = dto.getUserPwd();
            String encodedPwd = userEntity.getUserPwd(); // 암호화된 비밀번호
            boolean isMatched = passwordEncoder.matches(userPwd, encodedPwd); // 비밀번호 일치하는지 확인
            if(!isMatched) return SignInResponseDto.signInFail(); // 일치하지않으면

            token = jwtProvider.create(userId,userRole,60*60*10L); // 토큰 생성
            
        } catch (Exception e) {
            
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return SignInResponseDto.success(token);
    }// xx LoginFilter
}
