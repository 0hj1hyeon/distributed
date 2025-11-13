package com.example.distributed.repository;

import com.example.distributed.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 💡 JpaRepository를 상속받아 CRUD 기능을 자동으로 제공받음
public interface UserRepository extends JpaRepository<User, Long> {

    // 💡 사용자 ID로 User 객체 전체를 조회하는 메서드 정의 (JPA 쿼리 메서드)
    Optional<User> findByUsername(String username);
}