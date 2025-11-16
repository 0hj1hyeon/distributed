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

// 🔑 WebFlux 환경에서 테스트를 위해 RANDOM_PORT 설정
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTests {

    // 🔑 MockMvc 대신 WebTestClient 주입
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 테스트용 비밀번호 값 재정의 ---
    private static final String VALID_USER_ID = "valid_user";
    // 🔑 테스트 시 사용하는 실제 평문 비밀번호를 상수로 정의
    private static final String VALID_PASSWORD_PLAINTEXT = "correct_password_123";
    private static final String WRONG_PASSWORD_PLAINTEXT = "11111111_CompletelyDifferent";
    // ------------------------------------


    @TestConfiguration // 테스트 전용 설정 클래스
    static class TestDataSetup {
        @Bean
        public User testUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            // 🔑 DB에 저장할 때, VALID_PASSWORD_PLAINTEXT의 해시 값을 사용합니다.
            String encodedPassword = passwordEncoder.encode(VALID_PASSWORD_PLAINTEXT);

            // 🔑 DB에 저장할 사용자 엔티티 생성
            User testUser = new User(VALID_USER_ID, encodedPassword);

            // 🔑 사용자 데이터 저장 (테스트 시작 전에 실행됨)
            return userRepository.save(testUser);
        }
    }

    @Test
    @DisplayName("유효한_자격증명으로_로그인_요청에_성공해야_하고_JWT를_받아야_한다")
    void 유효한_자격증명으로_로그인_요청에_성공해야_하고_JWT를_받아야_한다() throws Exception {
        // Given
        // 🔑 VALID_PASSWORD_PLAINTEXT를 사용
        LoginRequest loginRequest = new LoginRequest(VALID_USER_ID, VALID_PASSWORD_PLAINTEXT);

        // When & Then
        webTestClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk() // 200 OK 예상
                .expectBody()
                .jsonPath("$.token").exists()
                .jsonPath("$.token").isNotEmpty();
    }

    @Test
    @DisplayName("잘못된_비밀번호로_로그인_시도_시_401_에러가_발생해야_한다")
    void 잘못된_비밀번호로_로그인_시도_시_401_에러가_발생해야_한다() throws Exception {
        // Given
        // 🔑 WRONG_PASSWORD_PLAINTEXT를 사용 (DB 해시 값과 달라야 함)
        LoginRequest wrongRequest = new LoginRequest(VALID_USER_ID, WRONG_PASSWORD_PLAINTEXT);

        // When & Then
        webTestClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(wrongRequest)
                .exchange()
                .expectStatus().isUnauthorized(); // 401 Unauthorized 예상
    }

    @Test
    @DisplayName("인증되지_않은_상태로_보호된_리소스에_접근_시_401_에러가_발생해야_한다")
    void 인증되지_않은_상태로_보호된_리소스에_접근_시_401_에러가_발생해야_한다() throws Exception {
        // Given: 보호된 임의의 엔드포인트 ("/api/protected")

        // When & Then
        webTestClient.post().uri("/api/protected")
                .exchange()
                .expectStatus().isUnauthorized(); // 401 Unauthorized 예상
    }
}