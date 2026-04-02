package com.hrtech.resume_screening.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * EmailService — Email sending is disabled.
 * OTP is shown directly on screen instead.
 * To enable real email: configure JavaMailSender
 * in application.yml and restore the original code.
 */
@Service
@Slf4j
public class EmailService {

    /**
     * Logs the OTP instead of sending an email.
     * The frontend displays the OTP directly on screen.
     */
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("╔══════════════════════════════════╗");
        log.info("║         OTP GENERATED            ║");
        log.info("╚══════════════════════════════════╝");
        log.info("Email : {}", toEmail);
        log.info("OTP   : {}", otp);
        log.info("Note  : Email sending disabled — OTP shown on screen");
        // OTP is returned to frontend and displayed on screen
        // No email is sent — this is intentional for development
    }
}
