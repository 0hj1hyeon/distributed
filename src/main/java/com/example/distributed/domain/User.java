package com.example.distributed.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user") // MySQL 테이블 이름 지정
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    private String username; // 사용자 ID (로그인 시 사용)

    private String password; // 암호화된 비밀번호

    // BCrypt 적용을 위해 초기 사용자를 미리 등록하는 편의 생성자
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}