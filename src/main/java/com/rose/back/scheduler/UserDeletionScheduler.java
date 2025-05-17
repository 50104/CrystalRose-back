package com.rose.back.scheduler;

import com.rose.back.domain.auth.repository.AuthRepository;
import com.rose.back.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDeletionScheduler {

    private final AuthRepository authRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteReservedUsers() {
        List<UserEntity> users = authRepository.findByReservedDeleteAtBefore(LocalDateTime.now());

        for (UserEntity user : users) {
            log.info("1주일 경과로 유저 삭제: {}", user.getUserId());
            authRepository.delete(user);
        }
    }
}
