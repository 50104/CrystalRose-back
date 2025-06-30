package com.rose.back.domain.diary.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rose.back.domain.diary.dto.CareLogRequest;
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

    public List<String> getCareDates(Long userNo) {
        return careLogRepository.findDistinctCareDatesByUserNo(userNo).stream()
            .map(LocalDate::toString)
            .toList();
    }

    public List<RoseCareLogDto> getAllLogs(Long userNo) {
        return careLogRepository.findByUserNo_UserNoOrderByCareDateDesc(userNo).stream()
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
        CareLogEntity existingEntity = careLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("관리 기록을 찾을 수 없습니다."));
        
        if (!existingEntity.getUserNo().getUserNo().equals(userNo)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }
        
        CareLogEntity updatedEntity = CareLogEntity.builder()
            .id(existingEntity.getId())
            .userNo(existingEntity.getUserNo())
            .careDate(request.careDate())
            .fertilizer(request.fertilizer())
            .pesticide(request.pesticide())
            .adjuvant(request.adjuvant())
            .compost(request.compost())
            .fungicide(request.fungicide())
            .watering(request.watering())
            .note(request.note())
            .build();
        careLogRepository.save(updatedEntity);
    }
}
