package com.rose.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    
    // @Repository 구현체를 컴포넌트로 올려주는 자바 빈으로 등록시켜줌.
}
