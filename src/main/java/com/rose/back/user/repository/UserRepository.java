package com.rose.back.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    // @Repository 구현체를 컴포넌트로 올려주는 자바 빈으로 등록시켜줌.

    UserEntity findByUserId(String userId);

    boolean existsByUserId(String userId);

    boolean existsByUserEmail(String userEmail);

    UserEntity findByUserEmail(String userEmail);
}
