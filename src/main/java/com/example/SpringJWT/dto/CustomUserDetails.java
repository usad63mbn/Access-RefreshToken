package com.example.SpringJWT.dto;

import com.example.SpringJWT.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayDeque;
import java.util.Collection;


/* UserDetails: Spring Security에서 사용자 인증 정보를 캡슐화를 위한 용도로 만든 인터페이스로, UsernamePasswordAuthenticationFilter에서 사용.
 * 사용자 계정의 정보(예: 사용자 이름, 암호화된 비밀번호, 권한 등)를 가져와 인증 및 권한 부여를 처리
 * 이름은 getUsername(), 비밀번호는 getPassword(), 권한은 getAuthorities() 통해 Filter에서 인증 로직 처리.
 */
public class CustomUserDetails implements UserDetails {
    private final UserEntity userEntity;

    public CustomUserDetails(UserEntity userEntity){
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayDeque<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userEntity.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }
}
