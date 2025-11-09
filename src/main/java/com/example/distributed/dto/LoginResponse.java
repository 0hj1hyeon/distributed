package com.example.distributed.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
}