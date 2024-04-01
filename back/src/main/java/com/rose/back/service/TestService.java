package com.rose.back.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rose.back.entity.TestEntity;
import com.rose.back.repository.TestRepository;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    public void saveTestEntity(TestEntity testEntity) {

        testRepository.save(testEntity);
    }
    
    public List<TestEntity> getAllTestEntities() {

        return testRepository.findAll();
    }
}
