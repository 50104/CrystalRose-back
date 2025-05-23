package com.rose.back.domain.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.report.entity.UserBlock;
import com.rose.back.domain.report.repository.UserBlockRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    public void blockUser(Long blockerId, Long targetId) { // 차단 요청
        UserEntity blocker = userRepository.findById(blockerId)
            .orElseThrow(() -> new EntityNotFoundException("차단자 없음"));
        UserEntity blocked = userRepository.findById(targetId)
            .orElseThrow(() -> new EntityNotFoundException("대상 없음"));

        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new IllegalStateException("이미 차단된 사용자입니다.");
        }

        UserBlock userBlock = new UserBlock();
        userBlock.setBlocker(blocker);
        userBlock.setBlocked(blocked);

        userBlockRepository.save(userBlock);
    }
}
