package com.example.distributed.filter;

import com.example.distributed.util.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class JwtAuthenticationFilter implements WebFilter {

    public static final String USER_ID_ATTRIBUTE = "X-User-Id";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        Optional<String> tokenOptional = resolveToken(exchange.getRequest().getHeaders());

        if (tokenOptional.isEmpty()) {
            return chain.filter(exchange);
        }

        String token = tokenOptional.get();

        if (jwtTokenProvider.validateToken(token)) {

            try {
                String userId = jwtTokenProvider.getUsername(token);
                exchange.getAttributes().put(USER_ID_ATTRIBUTE, userId);
            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or Expired JWT Token");
            }
        } else {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or Expired JWT Token");
        }

        return chain.filter(exchange);
    }

    private Optional<String> resolveToken(HttpHeaders headers) {
        String bearerToken = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }


    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        return exchange.getResponse().setComplete();
    }
}