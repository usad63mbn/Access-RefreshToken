package com.example.SpringJWT.service;

import com.example.SpringJWT.dto.JoinDTO;
import com.example.SpringJWT.entity.UserEntity;
import com.example.SpringJWT.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinDTO joinDTO){
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();

        Boolean isExist = userRepository.existsByUsername(username);

        if(isExist){
            return;
        }

        UserEntity data = new UserEntity();
        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password)); //암호화해서 저장
        data.setRole("ROLE_ADMIN");
        userRepository.save(data);

    }
}
/*
* BCryptPasswordEncoder는 원본 비밀번호와 암호화된 비밀번호를 비교하여 인증을 수행
* boolean matches(CharSequence rawPassword, String encodedPassword) 메서드를 사용하여 원본 비밀번호와 암호화된 비밀번호를 비교
* */