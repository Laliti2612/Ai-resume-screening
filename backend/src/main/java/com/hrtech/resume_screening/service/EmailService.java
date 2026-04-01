package com.hrtech.resume_screening.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation
        .Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail
        .MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail,
                             String otp) {
        try {
            MimeMessage message =
                    mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(
                    "🔐 AI Recruit — Password Reset OTP");
            helper.setText(buildEmailHtml(otp), true);

            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Email failed: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Email failed: " + e.getMessage());
        }
    }

    private String buildEmailHtml(String otp) {
        return "<!DOCTYPE html><html><body " +
                "style='font-family:Segoe UI,Arial," +
                "sans-serif;background:#f0f4ff;" +
                "padding:20px;'>" +
                "<div style='max-width:480px;" +
                "margin:0 auto;background:white;" +
                "border-radius:16px;overflow:hidden;" +
                "box-shadow:0 4px 24px " +
                "rgba(59,130,246,0.12);'>" +

                // Header
                "<div style='background:linear-gradient(" +
                "135deg,#0f1b34,#1e3a8a);" +
                "padding:28px;text-align:center;'>" +
                "<div style='font-size:32px;'>🤖</div>" +
                "<h2 style='color:white;margin:8px 0 0;" +
                "font-size:20px;'>AI Recruit</h2>" +
                "</div>" +

                // Body
                "<div style='padding:32px;'>" +
                "<h3 style='color:#1e293b;" +
                "margin:0 0 8px;'>" +
                "Password Reset OTP</h3>" +
                "<p style='color:#64748b;font-size:14px;" +
                "margin:0 0 24px;line-height:1.6;'>" +
                "Use the code below to reset your password." +
                " Valid for <strong>10 minutes</strong>." +
                "</p>" +

                // OTP Box
                "<div style='background:#f0f7ff;" +
                "border:2px dashed #3b82f6;" +
                "border-radius:12px;padding:24px;" +
                "text-align:center;margin:0 0 24px;'>" +
                "<p style='color:#64748b;font-size:11px;" +
                "font-weight:700;text-transform:uppercase;" +
                "letter-spacing:0.1em;margin:0 0 8px;'>" +
                "Your OTP Code</p>" +
                "<div style='font-size:40px;" +
                "font-weight:800;color:#1e3a8a;" +
                "letter-spacing:10px;" +
                "font-family:monospace;'>" +
                otp + "</div>" +
                "<p style='color:#94a3b8;font-size:11px;" +
                "margin:8px 0 0;'>" +
                "Expires in 10 minutes</p>" +
                "</div>" +

                // Warning
                "<div style='background:#fef9c3;" +
                "border-left:4px solid #f59e0b;" +
                "border-radius:6px;" +
                "padding:12px 16px;margin:0 0 20px;'>" +
                "<p style='color:#92400e;" +
                "font-size:13px;margin:0;'>" +
                "⚠️ If you did not request this, " +
                "ignore this email.</p>" +
                "</div>" +

                "<p style='color:#94a3b8;font-size:11px;" +
                "text-align:center;margin:0;'>" +
                "AI Resume Screening System v1.0" +
                "</p></div></div></body></html>";
    }
}