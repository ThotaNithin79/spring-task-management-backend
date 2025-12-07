package com.sribalajiads.task_management.controller;

import com.sribalajiads.task_management.dto.TaskStatsDTO;
import com.sribalajiads.task_management.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.sribalajiads.task_management.entity.Role;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.UserRepository; // Make sure this is injected

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            TaskStatsDTO stats = reportService.getEmployeeStats(email, startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Get Specific Employee Stats (Secure Logic)
    // This replaces any previous method mapped to "/employee/{userId}"
    @GetMapping("/employee/{userId}")
    public ResponseEntity<?> getEmployeeStatsById(
            @PathVariable Long userId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            // 1. Get Requester
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String requesterEmail = auth.getName();
            User requester = userRepository.findByEmail(requesterEmail)
                    .orElseThrow(() -> new RuntimeException("Requester not found"));

            // 2. Get Target User
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Target user not found"));

            // 3. AUTHORIZATION LOGIC
            boolean isAllowed = false;

            // Rule A: Self-Access
            if (requester.getId().equals(targetUser.getId())) {
                isAllowed = true;
            }
            // Rule B: Super Admin
            else if (requester.getRole() == Role.SUPER_ADMIN) {
                isAllowed = true;
            }
            // Rule C: Department Head (Same Dept)
            else if (requester.getRole() == Role.DEPT_HEAD) {
                if (requester.getDepartment() != null && targetUser.getDepartment() != null) {
                    if (requester.getDepartment().getId().equals(targetUser.getDepartment().getId())) {
                        isAllowed = true;
                    }
                }
            }

            if (!isAllowed) {
                return ResponseEntity.status(403).body("Access Denied: You do not have permission to view stats for this user.");
            }

            // 4. Generate Report
            TaskStatsDTO stats = reportService.getStatsByUserId(userId, startDate, endDate);
            return ResponseEntity.ok(stats);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}