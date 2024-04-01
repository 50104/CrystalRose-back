package com.rose.back.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TestEntity {
    
    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    public TestEntity(){

    }

    public TestEntity(String name) {

        this.name = name;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}