package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.AuthResponse;
import com.sribalajiads.task_management.dto.LoginRequest;
import com.sribalajiads.task_management.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import com.sribalajiads.task_management.entity.OtpVerification;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.OtpRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import com.sribalajiads.task_management.service.EmailService;
import com.sribalajiads.task_management.dto.PasswordResetRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Authenticate credentials with Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. If valid, load user details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // 3. Generate JWT Token
        final String jwt = jwtUtils.generateToken(userDetails);

        // 4. Return token
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    // 1. Forgot Password - Generate & Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (!userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("User not found with email: " + email);
        }

        String otp = emailService.generateOtp();

        // Save OTP to DB (Overwrites existing if any)
        OtpVerification verification = new OtpVerification(
                email,
                otp,
                LocalDateTime.now().plusMinutes(5) // Expires in 5 mins
        );
        otpRepository.save(verification);

        // Send Email
        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok("OTP sent to your email.");
    }

    // 2. Reset Password - Verify OTP & Update Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        // Find OTP record
        OtpVerification verification = otpRepository.findById(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Email or OTP not requested"));

        // Check if OTP matches
        if (!verification.getOtpCode().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        // Check if expired
        if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP Expired");
        }

        // Update User Password
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete used OTP
        otpRepository.delete(verification);

        return ResponseEntity.ok("Password reset successfully. You can now login.");
    }
}