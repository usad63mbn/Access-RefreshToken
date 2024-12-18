package com.example.SpringJWT.config;

import com.example.SpringJWT.jwt.CustomLogoutFilter;
import com.example.SpringJWT.jwt.JWTFilter;
import com.example.SpringJWT.jwt.JWTUtil;
import com.example.SpringJWT.jwt.LoginFilter;
import com.example.SpringJWT.repository.RefreshRepository;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

// 기본 설정을 위한 클래스에 붙으며, 해당 클래스는 Spring 컨테이너에서 빈으로 관리
@Configuration

// Spring Security를 활성화하고, 애플리케이션의 보안 구성을 사용자 정의할 수 있도록 해주는 어노테이션
@EnableWebSecurity
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    private JWTUtil jwtUtil;

    private final RefreshRepository refreshRepository;

    //DI(주입해줌, AuthenticationConfiguration class는 @Configuration이, JWTUtil에는 @Component가 붙음. )
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil, RefreshRepository refreshRepository) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }


    //AuthenticationManager Bean 등록
    /*  AuthenticationConfiguration과 AuthenticationManager는 인증(Authentication) 과정을 관리하기 위한 핵심 컴포넌트.
        동작 과정: 클라이언트 인증 요청(로그인) -> AuthenticationManager가 요청을 받아서 적절한 AuthenticationProvider에 위임.
                -> 인증이 성공하면, Authentication 객체를 반환, 실패하면 예외를 던짐
        AuthenticationManager를 생성하기 위해선, AuthenticationConfiguration 객체가 필요.
    */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }


    // 비밀번호 암호화를 위해 사용(Spring Security에서 제공)
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        /*
        * csrf: 웹 애플리케이션에서 발생할 수 있는 보안 취약 점 중 하나로, 클라이언트의 인증 정보를 탈취 및 악용하여 서버에 악의적인 요청을 보냄.
        * */
        //csrf보호를 비활성화.
        http.csrf((auth) -> auth.disable());

        //From 로그인 방식 disable(사용x), jwt 방식을 사용
        http.formLogin((auth) -> auth.disable());
        //http basic 인증 방식 disable
        http.httpBasic((auth) -> auth.disable());

        //세션 설정(STATELESS로 관리)
        // Spring Security Level 에서는 Session 사용 비활성화, Spring MVC Level에서 직접 사용하는 것은 가능.
        http.sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));



        /*
        * Spring Security는 SecurityFilterChain이라는 필터 체인을 통해 요청에 대한 다양한 검증과 인증 작업을 마친 후, Controller로 전달.
        * */
        /*
        * 경로별 인가 작업. 모든 요청은 인증된 사용자만 접근 가능하며, 인증되지 않은 사용자가 접근하면 401에러를 발생.
        * Spring Security는 역할 이름 앞에 "ROLE_"라는 접두사를 자동으로 붙임 -> hasRole("ADMIN")은 ROLE_ADMIN을 검사
        * */
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/login", "/", "/join").permitAll() //모든 사용자
                .requestMatchers("/admin").hasRole("ADMIN") //role이 ADMIN인 유저만
                .requestMatchers("/reissue").permitAll()
                .anyRequest().authenticated()); //나머지는 로그인한 유저만

        //필터 추가, 첫번재 인자의 필터를 두번째 인자로 온 필터자리에 대체함
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository ), UsernamePasswordAuthenticationFilter.class);

        // 해당 필터 사용하니까 /admin에 접속 가능
        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        // 커스텀한 로그아웃 필터 사용하기
        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        return http.build();
    }

}
// 로그인
/* 순서
* JWTFilter.doFilterInternal -> LoginFilter.attemptAuthentication -> CustomUserDetailService.loadUserByUsername ->
*  ...->DaoAuthenticationProvider(비밀번호 비교등의 인증 작업, 제일 중요)-> ...
* -> LoginFilter.successfulAuthentication/unsuccessfulAuthentication
* */

/* 역할
* attemptAuthentication: 인증 프로세스의 시작점, 보통 자격 증명을 추출. 사용자 정보를 담은 토큰을 Authentication 객체로 변환하여 AuthenticationManager로 전달(아직 필요한지 모르겠음)
* loadUserByUsername: 데이터베이스에서 사용자 정보(이름(ID), 암호화된 비밀번호, 권한)를 가져와서 Details객체에 담아서 리턴하기, Details객체의 password와 message body의 password를 다음 필터인 Dao에서 비교.
* DaoAuthenticationProvider: PasswordEncoder를 사용하여 비밀번호를 비교(해당 프로젝트에서도 BCryptPasswordEncoder bCryptPasswordEncoder를 통해 비밀번호 인코딩함)
*                            Spring Security는 클라이언트가 비밀번호를 body에 password에 담았다고 가정하고, 값을 추출(obtainPassword()사용), 그렇기에 key가 password가 아니면 비교 불가
* DaoAuthenticationProvider의 인증 결과에 따라 successfulAuthentication/unsuccessfulAuthentication 호출됨.
* */

// ----------------------------------
// /admin에 접근하기
/*
*  UsernamePasswordAuthenticationFilter.attemptAuthentication를 건너뛰고 다음 단계로 이동.
*  FilterSecurityInterceptor가 실행되어, 사용자의 권한(Role)이 ADMIN인지 확인.
*       확인하는 방법은 SecurityContextHolder에 인증된 Authentication 객체를 통해 확인
*       JWTFilter.doFilterInternal에서 SecurityContextHolder에 Authentication 객체를 저장했음.
* */