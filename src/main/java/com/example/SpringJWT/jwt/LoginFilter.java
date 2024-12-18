package com.example.SpringJWT.jwt;

import com.example.SpringJWT.dto.CustomUserDetails;
import com.example.SpringJWT.entity.RefreshEntity;
import com.example.SpringJWT.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    // 사용자로부터 아이디와 비밀번호를 받아 UsernamePasswordAuthenticationToken에 저장.
    // authenticationManager.authenticate(authToken)**를 호출하여 인증을 위임하면, AuthenticationManager와 Provider가
    // DaoAuthenticationProvider을 호출하며 해당 Provider는 UserDetailsService을 사용하여 사용자 정보를 로드.
    // 인증 성공 시, 인증 객체(Authentication)을 인자로 담아서 successfulAuthentication을 호출.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        //클라이언트 요청에서 username, password 추출(UsernamePasswordAuthenticationFilter에 obtainXXX메소드 있음)
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        System.out.println("LoginFilter.attemptAuthentication");
        System.out.println(username);

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함, 세번째 인자는 ROLE.
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        //token에 담은 후 검증을 위해 AuthenticationManager에 전달
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //유저 정보
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();


        //토큰 생성
        String access = jwtUtil.createJwt("access", username, role, 600000L); //10분
        String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L); //24시간


        // RefreshToken DB에 저장.
        Date date = new Date(System.currentTimeMillis() + 86400000L);
        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());
        refreshRepository.save(refreshEntity);


        // 각 토큰의 저장 장소를 다르게 두기
        response.setHeader("access", access); //access token은 Header.
        response.addCookie(createCookie("refresh", refresh));   // refresh token은 Cookie에 저장.
        response.setStatus(HttpStatus.OK.value());
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException failed) throws IOException, ServletException {
        System.out.println("LoginFilter.unsuccessfulAuthentication");

        response.setStatus(401);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

}

