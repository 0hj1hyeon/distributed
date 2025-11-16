package com.example.distributed.controller;

import com.example.distributed.dto.LoginRequest;
import com.example.distributed.dto.LoginResponse;
import com.example.distributed.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {

        return Mono.fromCallable(() ->
                        userService.authenticateAndGenerateToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                )
                .subscribeOn(Schedulers.boundedElastic())

                .map(jwtToken -> {
                    LoginResponse response = LoginResponse.builder()
                            .success(true)
                            .message("로그인 성공 및 JWT 토큰 발급")
                            .token(jwtToken)
                            .build();
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(RuntimeException.class, e -> {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage()));
                });
    }
}