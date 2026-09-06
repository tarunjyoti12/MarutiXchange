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

    public void sendOtpEmail(
            String toEmail, String otp) {
        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(
                    "MarutiXchange - OTP Verification");
            message.setText(
                    "Dear User,\n\n"
                            + "Your OTP for MarutiXchange"
                            + " verification is: "
                            + otp
                            + "\n\nThis OTP is valid"
                            + " for 10 minutes."
                            + "\nDo not share this OTP"
                            + " with anyone.\n\n"
                            + "Team MarutiXchange");
            mailSender.send(message);
            log.info("OTP email sent to: {}",
                    toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP: {}",
                    e.getMessage());
        }
    }

    public void sendPasswordResetEmail(
            String toEmail, String token) {
        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(
                    "MarutiXchange - Password Reset");
            message.setText(
                    "Dear User,\n\n"
                            + "Your password reset token is:\n"
                            + token
                            + "\n\nThis token is valid"
                            + " for 30 minutes."
                            + "\nIf you did not request this,"
                            + " please ignore.\n\n"
                            + "Team MarutiXchange");
            mailSender.send(message);
            log.info("Reset email sent to: {}",
                    toEmail);
        } catch (Exception e) {
            log.error("Failed to send reset email: {}",
                    e.getMessage());
        }
    }

    public void sendWelcomeEmail(
            String toEmail, String name) {
        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(
                    "Welcome to MarutiXchange!");
            message.setText(
                    "Dear " + name + ",\n\n"
                            + "Welcome to MarutiXchange!\n"
                            + "India's Trusted Maruti"
                            + " Resale Marketplace.\n\n"
                            + "Your account has been"
                            + " created successfully.\n\n"
                            + "Happy buying and selling!\n\n"
                            + "Team MarutiXchange");
            mailSender.send(message);
            log.info("Welcome email sent to: {}",
                    toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome: {}",
                    e.getMessage());
        }
    }
}