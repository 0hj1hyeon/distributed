package com.example.distributed.config;

// WebFlux 기반 Spring Security
import com.example.distributed.filter.JwtAuthenticationFilter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository; // 세션 미사용


import com.example.distributed.util.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebFluxSecurity // 👈 WebFlux Security 활성화
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 💡 WebFlux에서는 SecurityFilterChain 대신 SecurityWebFilterChain을 정의합니다.
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // 1. JWT 유효성 검증 필터 인스턴스 생성
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        http
                // WebFlux 환경에서 CSRF, HTTP Basic, Session 관리 설정
                .csrf((csrf) -> csrf.disable())
                .httpBasic((httpBasic) -> httpBasic.disable())

                // STATELESS 설정 (WebFlux에서는 SecurityContextRepository를 사용)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // 2. 권한 설정
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/api/login").permitAll() // 로그인 경로는 인증 없이 허용
                        .anyExchange().authenticated() // 나머지 모든 요청은 인증 필요
                )
        // 3. 필터 체인에 커스텀 필터 추가 (WebFlux 방식)
        // WebFlux는 http.addFilterBefore 대신, 필터를 등록하여 체인에 추가합니다.
        // 하지만 게이트웨이 환경이므로, 필터 등록은 GatewayFilterFactory에서 처리하거나
        // 또는 WebFilter로 직접 등록하는 것이 일반적입니다. (여기서는 JwtValidationWebFilter를 WebFilter로 사용하도록 가정)

        // WebFlux에서 필터를 등록하는 가장 일반적인 방법은 @Bean으로 등록하는 것입니다.
        // 여기서는 이미 SecurityConfig에 JWT 로직이 필요하므로, SecurityWebFilterChain에서 필터를 직접 정의하는 대신
        // @Bean으로 등록된 JwtValidationWebFilter가 자동으로 Security Chain에 포함되도록 하는 것이 일반적입니다.

        // 💡 여기서는 간결하게 설정하고, JwtValidationWebFilter가 @Component로 등록되거나 WebFlux 설정에 추가된다고 가정합니다.

        ;

        // 최종 SecurityWebFilterChain 반환
        return http.build();
    }

    // 💡 JwtValidationWebFilter를 Bean으로 등록합니다.
    @Bean
    public JwtAuthenticationFilter jwtValidationWebFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
}