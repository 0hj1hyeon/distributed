package com.example.distributed.controller;
// ... (필요한 import 생략)

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/externalService")
    public ResponseEntity<Map<String, Object>> externalServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "현재 시스템 부하로 인해 요청을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.");
        // 503 Service Unavailable 상태 코드를 반환하는 것이 가장 적절합니다.
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}