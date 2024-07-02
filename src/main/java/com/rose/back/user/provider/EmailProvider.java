package com.rose.back.user.provider;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailProvider {

    private final JavaMailSender javaMailSender;

    private final String SUBJECT = "[빛나는 크리스퇄 가드닝 인증메일입니다.]";

    // 이메일 인증 메일 전송
    public boolean sendCertificationMail(String email, String certificationNumber) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage(); // 메세지 객체(인스턴스) 생성
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            
            String htmlContent = getCertificationMessage(certificationNumber); // 인증 메시지를 HTML 형식으로 가져오는 메소드 호출

            messageHelper.setTo(email); // 수신자 이메일 설정
            messageHelper.setSubject(SUBJECT); // 이메일 제목 설정
            messageHelper.setText(htmlContent, true); // 이메일 내용 설정

            javaMailSender.send(message); // 이메일 전송
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getCertificationMessage(String certificationNumber) {
        String certificationMessage = "";
        certificationMessage += "<h1 style='text-align: center;'>[빛나는 크리스퇄 가드닝 인증메일입니다.]</h1>";
        certificationMessage += "<h3 style='text-align: center;'>인증코드 : <strong style='font-size: 32px; letter-spacing: 8px;'>" + certificationNumber + "</strong></h3>";
        return certificationMessage;
    }
}
