package com.rose.back.global.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailService {

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
        certificationMessage += "<h3 style='text-align: center;'><strong style='font-size: 32px; letter-spacing: 8px;'>" + certificationNumber + "</strong></h3>";
        return certificationMessage;
    }

    public void sendWithdrawalNotice(String email, String nickname) {
        String subject = "[빛나는 크리스퇄] 탈퇴 예정 안내";
        String body = """
            <h2 style='color:#333;'>안녕하세요, <b>%s</b>님.</h2>
            <p>회원 탈퇴 요청이 접수되었습니다.</p>
            <p style='color:red;'>계정은 7일 후 자동으로 삭제될 예정입니다.</p>
            <p>탈퇴를 철회하시려면 로그인 후 철회 버튼을 눌러주세요.</p>
            <br>
            <p style='font-size:12px; color:#999;'>※ 본 메일은 발신 전용입니다.</p>
            """.formatted(nickname);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML 형식 설정

            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
