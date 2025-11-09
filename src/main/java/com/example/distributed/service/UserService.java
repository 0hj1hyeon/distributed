package com.example.distributed.service;



import com.example.distributed.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    // 생성자 주입 (의존성 주입)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean authenticate(String username, String password) {

        String storedPassword = userRepository.findPasswordByUsername(username);

        if (storedPassword == null) {
            return false;
        }

        if (storedPassword.equals(password)) {
            System.out.println(username + "님이 로그인에 성공했습니다.");
            return true;
        } else {
            return false;
        }
    }
}
