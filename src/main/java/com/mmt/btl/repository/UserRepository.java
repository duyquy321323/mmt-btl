package com.mmt.btl.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsername(String username);
}