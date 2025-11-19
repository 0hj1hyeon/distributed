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
    private static final String USER_ID_HEADER = "X-User-Id";

    public AuthorizationHeaderFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            if (path != null && path.startsWith("/auth")) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
            }

            List<String> authorizationHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            String jwt = authorizationHeaders.get(0).replace("Bearer ", "");

            if (!jwtTokenProvider.validateToken(jwt)) {
                return onError(exchange, "JWT token is not valid or expired", HttpStatus.UNAUTHORIZED);
            }

            String userId = jwtTokenProvider.getUsername(jwt);
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(USER_ID_HEADER, userId)
                    .build();

            exchange.getResponse().getHeaders().add(USER_ID_HEADER, userId);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        System.err.println("API Gateway JWT Error: " + err);
        return response.setComplete();
    }

    public static class Config {
    }
}