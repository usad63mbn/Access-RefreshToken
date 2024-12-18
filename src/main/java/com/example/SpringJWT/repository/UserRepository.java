package com.example.SpringJWT.repository;

import com.example.SpringJWT.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/* JpaRepository를 상속받은 class는 Spring Data JPA에 의해 자동으로 구현,
 * @Component와 같은 별도의 어노테이션이 없어도, Spring Data JPA는 런타임 시에 프록시 객체를 자동으로 생성.
 */

// Spring Data JPA로, JPA를 기반으로 CRUD 및 쿼리 메서드를 자동으로 생성해주는 기술
// <T, K> 에는 Entity 클래스와 pk의 Referece type 선언.
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Boolean existsByUsername(String username);

    //username을 통해 회원을 조회
    UserEntity findByUsername(String username);
}
