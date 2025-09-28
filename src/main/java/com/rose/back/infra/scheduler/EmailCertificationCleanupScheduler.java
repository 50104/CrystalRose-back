package com.rose.back.infra.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.auth.repository.EmailCertificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class EmailCertificationCleanupScheduler {

    private final EmailCertificationRepository emailCertificationRepository;

    @Transactional
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void purgeExpiredUnusedCodes() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = 0;
        try {
            deleted = emailCertificationRepository.deleteAllExpiredAndUnused(now);
            if (deleted > 0) {
                log.info("[CERT-CLEANUP] 만료 미사용 인증코드 삭제 count={} at {}", deleted, now);
            } else {
                log.debug("[CERT-CLEANUP] 삭제 대상 없음 at {}", now);
            }
        } catch (Exception e) {
            log.error("[CERT-CLEANUP] 만료 인증코드 삭제 중 오류", e);
        }
    }
}
