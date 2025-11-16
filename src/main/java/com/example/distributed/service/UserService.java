package com.example.distributed.service;

import com.example.distributed.domain.User;
import com.example.distributed.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.distributed.util.JwtTokenProvider;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService implements ReactiveUserDetailsService {

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
            throw new RuntimeException("User not found.");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            System.out.println(username + "님이 로그인에 성공하고 토큰을 발급받았습니다.");
            return tokenProvider.createToken(username);
        } else {
            throw new RuntimeException("Invalid password.");
        }
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        return Mono.fromCallable(() -> userRepository.findByUsername(username))
                .flatMap(optionalUser -> optionalUser.<Mono<UserDetails>>map(user ->
                                Mono.just(org.springframework.security.core.userdetails.User.builder()
                                        .username(user.getUsername())
                                        .password(user.getPassword())
                                        .roles("USER")
                                        .build())
                        )
                        .orElseGet(() -> Mono.error(new UsernameNotFoundException("User not found: " + username))))
                .subscribeOn(Schedulers.boundedElastic());
    }
}