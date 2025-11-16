package com.example.distributed.filter;

import com.example.distributed.util.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

// WebFlux 환경에서 사용되는 필터입니다. 서블릿(Servlet) 관련 클래스를 사용하지 않습니다.
public class JwtAuthenticationFilter implements WebFilter {

    // JWT 검증 성공 시 사용자 ID를 저장할 상수 키
    public static final String USER_ID_ATTRIBUTE = "X-User-Id";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // 1. 요청 헤더에서 토큰 추출
        Optional<String> tokenOptional = resolveToken(exchange.getRequest().getHeaders());

        if (tokenOptional.isEmpty()) {
            // 토큰이 없으면 인증 처리 없이 다음 체인으로 진행 (Security 설정에 따라 익명 접근 처리)
            return chain.filter(exchange);
        }

        String token = tokenOptional.get();

        if (jwtTokenProvider.validateToken(token)) {
            // 2. 토큰 유효성 검증 성공 시
            try {
                // 사용자 ID 추출
                String userId = jwtTokenProvider.getUsername(token);

                // 3. 사용자 ID를 WebExchange 속성에 저장 (이후 Security/Controller에서 사용)
                exchange.getAttributes().put(USER_ID_ATTRIBUTE, userId);

            } catch (Exception e) {
                // 토큰은 있지만 유효하지 않거나 만료된 경우 (JWT 예외 처리)
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or Expired JWT Token");
            }
        } else {
            // 4. 유효성 검증 실패 시
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or Expired JWT Token");
        }

        // 5. 다음 필터/핸들러로 요청 전달
        return chain.filter(exchange);
    }

    // --- 유틸리티 메서드 ---

    // 💡 HTTP 헤더에서 'Bearer 토큰'을 추출하는 로직
    private Optional<String> resolveToken(HttpHeaders headers) {
        String bearerToken = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    // 💡 에러 발생 시 Mono<Void>를 반환하여 요청 처리 체인을 종료하고 에러 응답
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        // 에러 메시지 등을 포함한 응답 본문 작성이 필요할 수 있으나, 여기서는 간단히 상태 코드만 설정합니다.
        return exchange.getResponse().setComplete();
    }
}