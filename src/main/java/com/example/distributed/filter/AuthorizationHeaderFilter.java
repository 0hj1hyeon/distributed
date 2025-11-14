package com.example.distributed.filter;

import com.example.distributed.util.JwtTokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    // JwtTokenProvider는 @Component로 등록되어 있으므로 주입 가능
    public AuthorizationHeaderFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. 헤더에서 Authorization 토큰 확인
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
            }

            // Authorization: Bearer <token> 추출
            List<String> authorizationHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
                return onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = authorizationHeaders.get(0);
            String jwt = authorizationHeader.replace("Bearer ", "");

            // 2. 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(jwt)) {
                return onError(exchange, "JWT token is not valid or expired", HttpStatus.UNAUTHORIZED);
            }

            // 3. 토큰에서 사용자 ID 추출 및 요청 헤더에 추가 (내부 서비스 전달용)
            String userId = jwtTokenProvider.getUsername(jwt);

            // 요청을 내부 서비스로 전달하기 전에 사용자 ID를 헤더에 추가
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId) // 👈 내부 서비스가 사용할 사용자 ID 헤더
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    // 인증 실패 시 오류 응답을 반환하는 헬퍼 메서드
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        System.err.println("API Gateway JWT Error: " + err);
        return response.setComplete();
    }

    public static class Config {
        // 필터 설정이 필요하면 여기에 정의
    }
}