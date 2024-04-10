package com.rose.back.entity.wish;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.rose.back.entity.TestEntity;

//@Table(name = "my_table") 이런식으로 테이블 이름 지정 가능,
//지정 안하면 클래스 이름으로 자동지정됨
@Entity
public class WishEntity {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    //엔터티는 기본생성자가 꼭 필요함 (의존성 주입 해야하니까)
    // public TestEntity() {
    // }
}
