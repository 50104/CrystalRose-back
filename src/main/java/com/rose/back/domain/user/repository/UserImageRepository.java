package com.rose.back.domain.user.repository;

import com.rose.back.domain.user.entity.UserImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserImageRepository extends JpaRepository<UserImageEntity, Long> {

    List<UserImageEntity> findByUserUserNo(Long userNo);

    void deleteByUserUserNo(Long userNo);

    boolean existsByFileUrl(String fileUrl);
}
