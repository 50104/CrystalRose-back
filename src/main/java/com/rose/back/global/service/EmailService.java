package com.rose.back.global.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public boolean sendCertificationMail(String email, String certificationNumber) {
        String subject = "[DODOROSE] 메일인증 코드";
        String html = getCertificationHtml(certificationNumber);
        return send(email, subject, html);
    }

    public void sendWithdrawalNotice(String email, String userNick) {
        String subject = "[DODOROSE] 탈퇴 예정 안내";
        String html = getWithdrawalHtml(userNick);
        send(email, subject, html);
    }

    public void userIdFound(String email, String maskedUserId) {
        String subject = "[DODOROSE] 아이디 찾기 결과";
        String html = getUserIdHtml(maskedUserId);
        send(email, subject, html);
    }

    public void resetPassword(String email, String tempPassword) {
        String subject = "[DODOROSE] 임시 비밀번호 안내";
        String html = getTempPasswordHtml(tempPassword);
        send(email, subject, html);
    }

    private boolean send(String to, String subject, String html) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(message);
            log.info("메일 발송 완료 - 대상: {}", to);
            return true;
        } catch (Exception e) {
            log.error("메일 발송 실패 - 대상: {}, 에러: {}", to, e.getMessage(), e);
            return false;
        }
    }

    private String getCertificationHtml(String certificationNumber) {
        return """
        <div style="max-width:600px;margin:auto;padding:40px 30px;background:#fff;border-left:4px solid #4CAF50;font-family:Segoe UI,sans-serif;">
            <h1 style="font-size:24px;color:#222;margin-bottom:16px;">CRYSTAL GARDENING</h1>
            <hr style="border:none;border-top:1px solid #ddd;" />
            <h2 style="font-size:20px;color:#333;">이메일 인증 코드</h2>
            <p style="font-size:14px;color:#666;">아래 인증 코드를 입력해 이메일 인증을 완료해주세요.</p>
            <div style="font-size:28px;font-weight:bold;background-color:#f1f1f1;padding:16px;text-align:center;letter-spacing:10px;border-radius:8px;margin:24px 0;">%s</div>
            <p style="font-size:13px;color:#999;">만약 본인이 요청하지 않은 경우 즉시 비밀번호를 변경해주세요.</p>
            <p style="font-size:13px;color:#999;">인증 코드는 발송 후 5분간만 유효합니다.</p>
            <hr style="border:none;border-top:1px solid #eee;margin:40px 0 16px;" />
            <p style="font-size:12px;color:#bbb;text-align:center;">본 메일은 발신 전용입니다. | © 2025 DODOROSE</p>
        </div>
        """.formatted(certificationNumber);
    }

    private String getWithdrawalHtml(String nickname) {
        return """
        <div style="max-width:600px;margin:auto;padding:40px 30px;background:#fff;border-left:4px solid #fc9992;font-family:Segoe UI,sans-serif;">
            <h1 style="font-size:24px;color:#222;margin-bottom:16px;">CRYSTAL GARDENING</h1>
            <hr style="border:none;border-top:1px solid #ddd;" />
            <h2 style="font-size:20px;color:#333;">회원 탈퇴 예정 안내</h2>
            <p style="font-size:14px;color:#666;"><b>%s</b>님, 회원 탈퇴 요청이 정상적으로 접수되었습니다.</p>
            <div style="font-size:18px;font-weight:bold;background-color:#ffecec;color:#ff5555;padding:16px;text-align:center;border-radius:8px;margin:24px 0;">
                계정은 7일 후 자동 삭제 예정입니다.
            </div>
            <p style="font-size:13px;color:#999;">탈퇴를 원치 않으시면 로그인 후 <b>탈퇴 철회</b> 버튼을 눌러주세요.</p>
            <hr style="border:none;border-top:1px solid #eee;margin:40px 0 16px;" />
            <p style="font-size:12px;color:#bbb;text-align:center;">본 메일은 발신 전용입니다. | © 2025 DODOROSE</p>
        </div>
        """.formatted(nickname);
    }

    private String getUserIdHtml(String maskedUserId) {
        return """
        <div style="max-width:600px;margin:auto;padding:40px 30px;background:#fff;border-left:4px solid #2196f3;font-family:Segoe UI,sans-serif;">
            <h1 style="font-size:24px;color:#222;margin-bottom:16px;">CRYSTAL GARDENING</h1>
            <hr style="border:none;border-top:1px solid #ddd;" />
            <h2 style="font-size:20px;color:#222;">아이디 찾기 안내</h2>
            <p style="font-size:14px;color:#666;">회원님의 아이디는 다음과 같습니다:</p>
            <div style="font-size:20px;font-weight:bold;background-color:#e3f2fd;color:#0d47a1;padding:12px;text-align:center;border-radius:6px;margin:20px 0;">%s</div>
            <hr style="border:none;border-top:1px solid #eee;margin:40px 0 16px;" />
            <p style="font-size:12px;color:#bbb;text-align:center;">본 메일은 발신 전용입니다. | © 2025 DODOROSE</p>
        </div>
        """.formatted(maskedUserId);
    }

    private String getTempPasswordHtml(String tempPassword) {
        return """
        <div style="max-width:600px;margin:auto;padding:40px 30px;background:#fff;border-left:4px solid #ff9800;font-family:Segoe UI,sans-serif;">
            <h1 style="font-size:24px;color:#222;margin-bottom:16px;">CRYSTAL GARDENING</h1>
            <hr style="border:none;border-top:1px solid #ddd;" />
            <h2 style="font-size:20px;color:#222;">임시 비밀번호 발급 안내</h2>
            <p style="font-size:14px;color:#666;">회원님의 임시 비밀번호는 다음과 같습니다:</p>
            <div style="font-size:20px;font-weight:bold;background-color:#fff3e0;color:#ef6c00;padding:12px;text-align:center;border-radius:6px;margin:20px 0;">%s</div>
            <p style="font-size:13px;color:#999;">로그인 후 반드시 새 비밀번호로 변경해주세요.</p>
            <hr style="border:none;border-top:1px solid #eee;margin:40px 0 16px;" />
            <p style="font-size:12px;color:#bbb;text-align:center;">본 메일은 발신 전용입니다. | © 2025 DODOROSE</p>
        </div>
        """.formatted(tempPassword);
    }
}
