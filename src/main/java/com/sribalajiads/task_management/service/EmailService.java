package com.sribalajiads.task_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Random;

@Service
@EnableAsync
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(otp);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@sribalajiads.com"); // Your sender email
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP");
            message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP expires in 5 minutes.");

            mailSender.send(message);
            System.out.println("📧 Email sent successfully to " + toEmail);
        } catch (Exception e) {
            // Fallback for testing if SMTP is not configured
            System.err.println("⚠️ SMTP Error: " + e.getMessage());
            System.out.println("🛠️ DEBUG OTP: " + otp); // Print to console for testing
        }
    }

    @Async // Runs in a separate thread
    public void sendTaskAssignmentEmail(String toEmail, String assigneeName, String taskTitle, String creatorName, String description) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@sribalajiads.com");
            message.setTo(toEmail);
            message.setSubject("New Task Assigned: " + taskTitle);

            String body = "Hello " + assigneeName + ",\n\n" +
                    "A new task has been assigned to you by " + creatorName + ".\n\n" +
                    "Task: " + taskTitle + "\n" +
                    "Description: " + description + "\n\n" +
                    "Please log in to the portal to view details and start working.\n\n" +
                    "Regards,\nTask Management System";

            message.setText(body);

            mailSender.send(message);
            System.out.println("📧 Notification sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send notification email: " + e.getMessage());
        }
    }
}