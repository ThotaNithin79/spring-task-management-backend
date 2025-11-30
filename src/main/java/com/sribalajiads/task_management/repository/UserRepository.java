package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom method to find a user by their email (Used for Login)
    Optional<User> findByEmail(String email);

    // Custom method to check if an email already exists (Used during creation)
    boolean existsByEmail(String email);

    // Custom method to check if a username exists
    boolean existsByUsername(String username);
}