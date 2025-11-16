package com.example.distributed.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
}