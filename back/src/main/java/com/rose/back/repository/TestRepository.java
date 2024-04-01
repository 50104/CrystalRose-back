package com.rose.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.entity.TestEntity;

public interface TestRepository extends JpaRepository<TestEntity,String> {

    
}
