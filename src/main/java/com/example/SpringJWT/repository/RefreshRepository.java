package com.example.SpringJWT.repository;

import com.example.SpringJWT.entity.RefreshEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
* 로그인(LoginFilter) 시에 RefreshToken을 저장하고, AceessToken 재발급 시(JWTFilter) RereshToken을 조회 및 삭제 + 추가(Rotate)
* */
public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}
