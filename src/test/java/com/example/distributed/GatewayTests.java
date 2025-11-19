package com.example.distributed;

import com.example.distributed.domain.User;
import com.example.distributed.repository.UserRepository;
import com.example.distributed.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GatewayTests {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String VALID_USER_ID = "valid_user";
    private static final String VALID_PASSWORD_PLAINTEXT = "correct_password_123";

    private static String VALID_TOKEN;
    private static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ2YWxpZF91c2VyIiwiYXV0aCI6IlVTRVIiLCJpYXQiOjE2NzMwODgwMDAsImV4cCI6MTY3MzA4ODAwMH0.fake_expired_signature_12345678901234567890123456789012345678901234567890";

    private static final String PROTECTED_SERVICE_URI = "/api/protected-service";
    private static final String AUTH_SERVICE_URI = "/auth/login";



    @TestConfiguration
    static class TestDataSetup {

        @Bean
        public User testUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            String encodedPassword = passwordEncoder.encode(VALID_PASSWORD_PLAINTEXT);
            User testUser = new User(VALID_USER_ID, encodedPassword);
            return userRepository.save(testUser);
        }
    }


    @BeforeAll
    void setupTokens() {
        VALID_TOKEN = jwtTokenProvider.createToken(VALID_USER_ID);
    }

    @Test
    @DisplayName("유효한_JWT_토큰_제공_시_X_User_Id_헤더를_내부_서비스에_전달해야_한다")
    void 유효한_JWT_토큰_제공_시_X_User_Id_헤더를_내부_서비스에_전달해야_한다() {
        webClient.get().uri(PROTECTED_SERVICE_URI)
                .header("Authorization", "Bearer " + VALID_TOKEN)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().valueEquals("X-User-Id", VALID_USER_ID);
    }

    @Test
    @DisplayName("만료된_토큰을_포함하여_요청할_때_필터가_401_응답을_반환해야_한다")
    void 만료된_토큰을_포함하여_요청할_때_필터가_401_응답을_반환해야_한다() {
        webClient.get().uri(PROTECTED_SERVICE_URI)
                .header("Authorization", "Bearer " + EXPIRED_TOKEN)
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    @DisplayName("라우팅_규칙에_따라_정확한_서비스로_라우팅되어야_한다")
    void 라우팅_규칙에_따라_정확한_서비스로_라우팅되어야_한다() {
        webClient.get().uri(AUTH_SERVICE_URI)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}