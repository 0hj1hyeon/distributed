package com.example.distributed;

import com.example.distributed.domain.User;
import com.example.distributed.dto.LoginRequest;
import com.example.distributed.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTests {


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_USER_ID = "valid_user";
    private static final String VALID_PASSWORD_PLAINTEXT = "correct_password_123";
    private static final String WRONG_PASSWORD_PLAINTEXT = "11111111_CompletelyDifferent";


    @TestConfiguration
    static class TestDataSetup {
        @Bean
        public User testUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {

            String encodedPassword = passwordEncoder.encode(VALID_PASSWORD_PLAINTEXT);


            User testUser = new User(VALID_USER_ID, encodedPassword);


            return userRepository.save(testUser);
        }
    }

    @Test
    @DisplayName("유효한_자격증명으로_로그인_요청에_성공해야_하고_JWT를_받아야_한다")
    void 유효한_자격증명으로_로그인_요청에_성공해야_하고_JWT를_받아야_한다() throws Exception {

        LoginRequest loginRequest = new LoginRequest(VALID_USER_ID, VALID_PASSWORD_PLAINTEXT);
      webTestClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").exists()
                .jsonPath("$.token").isNotEmpty();
    }

    @Test
    @DisplayName("잘못된_비밀번호로_로그인_시도_시_401_에러가_발생해야_한다")
    void 잘못된_비밀번호로_로그인_시도_시_401_에러가_발생해야_한다() throws Exception {

        LoginRequest wrongRequest = new LoginRequest(VALID_USER_ID, WRONG_PASSWORD_PLAINTEXT);

        webTestClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(wrongRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("인증되지_않은_상태로_보호된_리소스에_접근_시_401_에러가_발생해야_한다")
    void 인증되지_않은_상태로_보호된_리소스에_접근_시_401_에러가_발생해야_한다() throws Exception {

        webTestClient.post().uri("/api/protected")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}