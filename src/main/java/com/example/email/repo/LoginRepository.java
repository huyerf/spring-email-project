package com.example.email.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.email.model.Login;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Optional<Login> findByLaoEmail(String laoEmail);
}

