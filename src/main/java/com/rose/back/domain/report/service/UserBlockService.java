package com.rose.back.domain.report.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.report.dto.UserSummaryDto;
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

    public List<UserSummaryDto> getBlockedUsers(Long blockerId) { // 차단자 ID로 차단된 사용자 목록 조회
        UserEntity blocker = userRepository.findById(blockerId)
            .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));

        return userBlockRepository.findAllByBlocker(blocker).stream()
            .map(block -> {
                UserEntity u = block.getBlocked();
                String profileImg = u.getUserProfileImg() != null ? u.getUserProfileImg() : "";
                return new UserSummaryDto(u.getUserNo(), u.getUserNick(), profileImg);
            }).toList();
    }

    public void unblockUser(Long blockerId, Long targetId) { // 차단 해제 요청
        UserEntity blocker = userRepository.findById(blockerId)
            .orElseThrow(() -> new EntityNotFoundException("사용자 없음"));
        UserEntity blocked = userRepository.findById(targetId)
            .orElseThrow(() -> new EntityNotFoundException("대상 없음"));

        userBlockRepository.deleteByBlockerAndBlocked(blocker, blocked);
    }
}
