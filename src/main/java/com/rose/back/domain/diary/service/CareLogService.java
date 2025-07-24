package com.rose.back.domain.diary.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rose.back.domain.diary.dto.CareLogRequest;
import com.rose.back.domain.diary.dto.CareLogResponse;
import com.rose.back.domain.diary.dto.RoseCareLogDto;
import com.rose.back.domain.diary.entity.CareLogEntity;
import com.rose.back.domain.diary.repository.CareLogRepository;
import com.rose.back.domain.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private final CareLogRepository careLogRepository;

    public void save(CareLogRequest request, Long userNo) {
        UserEntity user = UserEntity.builder()
            .userNo(userNo)
            .build();

        CareLogEntity entity = CareLogEntity.builder()
            .userNo(user)
            .careDate(request.careDate())
            .fertilizer(request.fertilizer())
            .pesticide(request.pesticide())
            .adjuvant(request.adjuvant())
            .compost(request.compost())
            .fungicide(request.fungicide())
            .watering(request.watering())
            .note(request.note())
            .build();
            
        careLogRepository.save(entity);
    }

    public List<CareLogResponse> getAllByUser(Long userNo) {
        return careLogRepository.findByUserNo_UserNoOrderByCareDateDesc(userNo)
            .stream()
            .map(log -> new CareLogResponse(
                log.getCareDate(),
                log.getWatering(),
                log.getFertilizer(),
                log.getPesticide(),
                log.getAdjuvant(),
                log.getFungicide(),
                log.getCompost(),
                log.getNote()
            ))
            .toList();
    }
    
    // 날짜 범위로 케어 로그 조회 (통합 API용)
    public List<RoseCareLogDto> getAllLogsByDateRange(Long userNo, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        return careLogRepository.findByUserNo_UserNoAndCareDateBetweenOrderByCareDateDesc(userNo, start, end).stream()
            .map(log -> new RoseCareLogDto(
                log.getId(),
                log.getFertilizer(),
                log.getPesticide(),
                log.getAdjuvant(),
                log.getCompost(),
                log.getFungicide(),
                log.getWatering(),
                log.getNote(),
                log.getCareDate()
            ))
            .toList();
    }

    public void update(Long id, CareLogRequest request, Long userNo) {
        CareLogEntity entity = careLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("관리 기록을 찾을 수 없습니다."));
        
        if (!entity.getUserNo().getUserNo().equals(userNo)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }
        
        entity.setCareDate(request.careDate());
        entity.setFertilizer(request.fertilizer());
        entity.setPesticide(request.pesticide());
        entity.setAdjuvant(request.adjuvant());
        entity.setCompost(request.compost());
        entity.setFungicide(request.fungicide());
        entity.setWatering(request.watering());
        entity.setNote(request.note());

        careLogRepository.save(entity);
    }

    public CareLogResponse getByDate(Long roseId, LocalDate date, Long userNo) {
        CareLogEntity entity = careLogRepository.findByUserNo_UserNoAndCareDate(userNo, date)
                .orElseThrow(() -> new RuntimeException("해당 날짜에 대한 관리 기록이 없습니다."));
        return CareLogResponse.fromEntity(entity);
    }
}
