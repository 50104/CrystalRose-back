package com.rose.back.infra.scheduler;

import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiModificationRequest;
import com.rose.back.domain.wiki.repository.WikiRepository;
import com.rose.back.domain.wiki.repository.WikiModificationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedWikiModificationCleaner {

    private final WikiModificationRequestRepository wikiModificationRequestRepository;
    private final WikiRepository wikiRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteRejectedWikiAndModificationRequests() {
        LocalDate fourDaysAgoDate = LocalDate.now().minusDays(4);
        LocalDateTime fourDaysAgoMidnight = fourDaysAgoDate.atTime(LocalTime.MIDNIGHT);
        
        log.info("거절된 Wiki 및 Wiki 수정 요청 정리 작업 시작 - 기준 날짜: {}", 
                fourDaysAgoMidnight);
        
        try {
            deleteRejectedModificationRequests(fourDaysAgoMidnight);
            deleteRejectedWikis(fourDaysAgoMidnight);
            
        } catch (Exception e) {
            log.error("거절된 Wiki 및 Wiki 수정 요청 정리 중 오류 발생", e);
            throw e;
        }
    }
    
    private void deleteRejectedModificationRequests(LocalDateTime deadline) {
        List<WikiModificationRequest> expiredRequests = 
            wikiModificationRequestRepository.findByStatusAndModifiedDateBefore(
                WikiModificationRequest.Status.REJECTED, 
                deadline
            );
        
        if (expiredRequests.isEmpty()) {
            log.info("삭제할 거절된 Wiki 수정 요청이 없습니다.");
            return;
        }
        log.info("삭제 대상 거절된 Wiki 수정 요청 개수: {}", expiredRequests.size());
        
        for (WikiModificationRequest request : expiredRequests) {
            log.debug("거절된 Wiki 수정 요청 삭제 - ID: {}, 원본 Wiki ID: {}, 요청자: {}, 거절 날짜: {})", 
                request.getId(), 
                request.getOriginalWiki().getId(),
                request.getRequester().getUserNick(),
                request.getModifiedDate());
            
            wikiModificationRequestRepository.delete(request);
        }
        log.info("거절된 Wiki 수정 요청 정리 완료 - 삭제된 요청 수: {}", expiredRequests.size());
    }
    
    private void deleteRejectedWikis(LocalDateTime deadline) {
        List<WikiEntity> expiredWikis = 
            wikiRepository.findRejectedWikisBeforeDeadline(deadline);
        
        if (expiredWikis.isEmpty()) {
            log.info("삭제할 거절된 Wiki가 없습니다.");
            return;
        }
        log.info("삭제 대상 거절된 Wiki 개수: {}", expiredWikis.size());
        
        for (WikiEntity wiki : expiredWikis) {
            log.debug("거절된 Wiki 삭제 - ID: {}, 이름: {}, 작성자 ID: {}, 거절 날짜: {})", 
                wiki.getId(), 
                wiki.getName(),
                wiki.getCreatedBy(),
                wiki.getModifiedDate());
            
            wikiRepository.delete(wiki);
        }
        log.info("거절된 Wiki 정리 완료 - 삭제된 Wiki 수: {}", expiredWikis.size());
    }
}