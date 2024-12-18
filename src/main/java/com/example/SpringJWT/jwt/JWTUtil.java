package com.example.SpringJWT.jwt;

import io.jsonwebtoken.Jwts;
import lombok.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private SecretKey secretKey;

    public JWTUtil(){
        String secret = "vmfhaltmskdlstkfkdgodyroqkfwkdbalroqkfwkdb alaaaaaaaaaaaaaaaabbbbb";
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    //jwt 생성
    public String createJwt(String category, String username, String role, Long expiredMs) {

        return Jwts.builder() //빌더 패턴 제공
                .claim("category",category)
                .claim("username", username) //사용자 지정 클레임, 다양한 데이터를 추가 할 수있음.
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) //발급 시간 설정
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) //토큰 만료시간 설정
                .signWith(secretKey) // JWT 서명을 위해 비밀 키를 지정
                .compact(); //compact: JWT를 문자열 형태로 직렬화
    }

    public String getCategory(String token){

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

}
