package com.example.distributed.repository;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final Map<String, String> userDb = new HashMap<>();

    public UserRepository() {
        userDb.put("test", "1234");
        userDb.put("admin", "admin123");
    }

    public String findPasswordByUsername(String username) {
        return userDb.get(username);
    }
}