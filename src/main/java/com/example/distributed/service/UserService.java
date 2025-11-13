package com.example.distributed.service;

import com.example.distributed.domain.User;
import com.example.distributed.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.distributed.util.JwtTokenProvider;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JwtTokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticateAndGenerateToken(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return null;
        }
        if (passwordEncoder.matches(password, user.getPassword())) {
            System.out.println(username + "님이 로그인에 성공하고 토큰을 발급받았습니다.");
            return tokenProvider.createToken(username);
        } else {
            return null;
        }
    }
}
