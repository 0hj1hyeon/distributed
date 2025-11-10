package com.example.distributed.controller;

import com.example.distributed.dto.LoginRequest;
import com.example.distributed.dto.LoginResponse;
import com.example.distributed.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginApiController {

    private final UserService userService;

    public LoginApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        String jwtToken = userService.authenticateAndGenerateToken(
                request.getUsername(),
                request.getPassword()
        );

        if (jwtToken != null) {
            LoginResponse response = LoginResponse.builder()
                    .success(true)
                    .message("로그인 성공 및 JWT 토큰 발급")
                    .token(jwtToken)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            LoginResponse response = LoginResponse.builder()
                    .success(false)
                    .message("아이디 또는 비밀번호가 잘못되었습니다.")
                    .build();

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

}