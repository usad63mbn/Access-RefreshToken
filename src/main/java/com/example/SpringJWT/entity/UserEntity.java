package com.example.SpringJWT.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

//@Entity: 데이터베이스 테이블과 매핑된 객체임을 나타냄, 데이터베이스 테이블의 구조를 정의하며 JPA를 통해 데이터베이스와 상호작용
// Entity: JPA에서 사용하는 어노테이션으로, 테이블과 매핑된 클래스 나타냄.
@Entity
@Setter
@Getter
public class UserEntity {

    @Id
    // @GeneratedValue: JPA에서 기본 키 값을 자동으로 생성할 때 사용.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String username;
    private String password;

    private String role;
}