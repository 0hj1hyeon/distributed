package com.example.distributed.service;

import com.example.distributed.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.example.distributed.util.JwtTokenProvider;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    public UserService(UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public String authenticateAndGenerateToken(String username, String password) {

        String storedPassword = userRepository.findPasswordByUsername(username);

        if (storedPassword == null) {
            return null;
        }

        if (storedPassword.equals(password)) {
            System.out.println(username + "님이 로그인에 성공하고 토큰을 발급받았습니다.");
            return tokenProvider.createToken(username);
        } else {
            return null;
        }
    }
}
