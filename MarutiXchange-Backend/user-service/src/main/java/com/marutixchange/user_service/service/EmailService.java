package com.marutixchange.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        sendEmail(toEmail,
                "MarutiXchange - OTP Verification",
                "Your OTP is: " + otp + "\nValid for 10 minutes.");
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        sendEmail(toEmail,
                "MarutiXchange - Password Reset",
                "Reset token: " + token + "\nValid for 30 minutes.");
    }

    public void sendWelcomeEmail(String toEmail, String name) {
        sendEmail(toEmail,
                "Welcome to MarutiXchange!",
                "Welcome " + name + "!\nHappy buying and selling!");
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Email sending failed: {}", e.getMessage());
        }
    }
}