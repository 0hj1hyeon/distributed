package com.example.distributed.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final Map<String, String> userDb = new HashMap<>();

    public UserRepository(PasswordEncoder passwordEncoder) {
        userDb.put("test", passwordEncoder.encode("1234"));
        userDb.put("admin", passwordEncoder.encode("admin123"));
    }

    public String findPasswordByUsername(String username) {
        return userDb.get(username);
    }
}