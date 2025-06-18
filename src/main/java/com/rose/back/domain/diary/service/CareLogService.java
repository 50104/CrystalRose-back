package com.rose.back.domain.diary.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rose.back.domain.diary.controller.CareLogController;
import com.rose.back.domain.diary.entity.CareLogEntity;
import com.rose.back.domain.diary.repository.CareLogRepository;
import com.rose.back.domain.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CareLogService {

    private final CareLogRepository careLogRepository;

    public void save(CareLogController.CareLogRequest request, Long userNo) {
        CareLogEntity entity = new CareLogEntity();
        UserEntity user = new UserEntity();
        user.setUserNo(userNo);

        entity.setUserNo(user); 
        entity.setCareDate(request.careDate());
        entity.setFertilizer(request.fertilizer());
        entity.setPesticide(request.pesticide());
        entity.setAdjuvant(request.adjuvant());
        entity.setCompost(request.compost());
        entity.setFungicide(request.fungicide());
        entity.setNote(request.note());
        careLogRepository.save(entity);
    }

    public List<String> getCareDates(Long userNo) {
        return careLogRepository.findDistinctCareDatesByUserNo(userNo).stream()
            .map(LocalDate::toString)
            .toList();
    }

    public List<CareLogController.RoseCareLogDto> getAllLogs(Long userNo) {
        return careLogRepository.findByUserNo_UserNoOrderByCareDateDesc(userNo).stream()
            .map(log -> new CareLogController.RoseCareLogDto(
                log.getId(),
                log.getFertilizer(),
                log.getPesticide(),
                log.getAdjuvant(),
                log.getCompost(),
                log.getFungicide(),
                log.getNote(),
                log.getCareDate()
            ))
            .toList();
    }
}
