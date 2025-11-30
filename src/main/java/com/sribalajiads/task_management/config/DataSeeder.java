package com.sribalajiads.task_management.config;

import com.sribalajiads.task_management.entity.Role;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if users table is empty
        if (userRepository.count() == 0) {
            // FIX: Using setters instead of Builder
            User admin = new User();
            admin.setUsername("SuperAdmin");
            admin.setEmail("admin@company.com");
            admin.setPassword(passwordEncoder.encode("password")); // Default password
            admin.setRole(Role.SUPER_ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            System.out.println("✅ Data Seeding Completed: Super Admin created with email 'admin@company.com' and password 'password'");
        }
    }
}