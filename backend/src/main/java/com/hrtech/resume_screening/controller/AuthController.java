package com.hrtech.resume_screening.controller;

import com.hrtech.resume_screening.entity.HrUser;
import com.hrtech.resume_screening.entity.PasswordResetOtp;
import com.hrtech.resume_screening.repository.HrUserRepository;
import com.hrtech.resume_screening.repository.PasswordResetOtpRepository;
import com.hrtech.resume_screening.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

    private final HrUserRepository           hrUserRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final JwtUtil                    jwtUtil;
    private final PasswordEncoder            passwordEncoder;

    public AuthController(
            HrUserRepository hrUserRepository,
            PasswordResetOtpRepository otpRepository,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.hrUserRepository = hrUserRepository;
        this.otpRepository    = otpRepository;
        this.jwtUtil          = jwtUtil;
        this.passwordEncoder  = passwordEncoder;
    }

    // ── Login ─────────────────────────────────────────────────
    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Map<String, String> data) {

        String email    = data.get("username");
        String password = data.get("password");
        Map<String, Object> response = new HashMap<>();

        log.info("LOGIN: {}", email);

        if (email == null || email.isBlank()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return response;
        }
        if (password == null || password.isBlank()) {
            response.put("success", false);
            response.put("message", "Password is required");
            return response;
        }

        try {
            HrUser user = findUser(email);

            if (user == null) {
                response.put("success", false);
                response.put("message",
                        "Invalid email or password");
                return response;
            }

            boolean passwordMatch;
            if (user.getPassword().startsWith("$2a$")
                    || user.getPassword().startsWith("$2b$")) {
                passwordMatch = passwordEncoder.matches(
                        password, user.getPassword());
            } else {
                passwordMatch = user.getPassword()
                        .equals(password);
                if (passwordMatch) {
                    user.setPassword(
                            passwordEncoder.encode(password));
                    hrUserRepository.save(user);
                    log.info("Password upgraded: {}",
                            user.getEmail());
                }
            }

            if (passwordMatch) {
                String token = jwtUtil.generateToken(
                        user.getEmail(), user.getRole());
                log.info("Login OK: {}", user.getEmail());
                response.put("success", true);
                response.put("token",   token);
                response.put("message", "Login Successful");
                response.put("user", Map.of(
                        "email", user.getEmail(),
                        "name",  user.getName(),
                        "role",  user.getRole()
                ));
            } else {
                response.put("success", false);
                response.put("message",
                        "Invalid email or password");
            }
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            response.put("success", false);
            response.put("message",
                    "Login error: " + e.getMessage());
        }
        return response;
    }

    // ── Register ──────────────────────────────────────────────
    @PostMapping("/register")
    public Map<String, Object> register(
            @RequestBody Map<String, String> data) {

        String name     = data.get("name");
        String email    = data.get("email");
        String password = data.get("password");
        Map<String, Object> response = new HashMap<>();

        log.info("REGISTER: {}", email);

        if (email == null || email.isBlank()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return response;
        }
        if (password == null || password.length() < 6) {
            response.put("success", false);
            response.put("message",
                    "Password must be 6+ characters");
            return response;
        }

        try {
            boolean exists = hrUserRepository
                    .existsByEmailIgnoreCase(email.trim());

            if (exists) {
                response.put("success", false);
                response.put("message",
                        "Email already registered.");
                return response;
            }

            HrUser newUser = new HrUser();
            newUser.setName(name != null && !name.isBlank()
                    ? name.trim() : "HR Manager");
            newUser.setEmail(email.trim().toLowerCase());
            newUser.setPassword(
                    passwordEncoder.encode(password));
            newUser.setRole("HR");
            hrUserRepository.save(newUser);

            String token = jwtUtil.generateToken(
                    newUser.getEmail(), newUser.getRole());

            log.info("Registered: {}", newUser.getEmail());

            response.put("success", true);
            response.put("token",   token);
            response.put("message",
                    "Account created successfully");
            response.put("user", Map.of(
                    "name",  newUser.getName(),
                    "email", newUser.getEmail(),
                    "role",  newUser.getRole()
            ));
        } catch (Exception e) {
            log.error("Register error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Forgot Password ───────────────────────────────────────
    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(
            @RequestBody Map<String, String> data) {

        String email = data.get("email");
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.isBlank()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return response;
        }

        try {
            HrUser user = findUser(email.trim());

            if (user == null) {
                response.put("success", false);
                response.put("message",
                        "No account found with this email");
                return response;
            }

            otpRepository.deleteByEmail(user.getEmail());

            String otp = String.format("%06d",
                    new Random().nextInt(999999));

            PasswordResetOtp otpEntity = new PasswordResetOtp();
            otpEntity.setEmail(user.getEmail());
            otpEntity.setOtp(otp);
            otpEntity.setExpiresAt(
                    LocalDateTime.now().plusMinutes(10));
            otpEntity.setUsed(false);
            otpRepository.save(otpEntity);

            log.info("OTP for {}: {}", user.getEmail(), otp);

            // Return OTP directly — no email needed
            response.put("success", true);
            response.put("email",   user.getEmail());
            response.put("otp",     otp);
            response.put("message",
                    "OTP generated successfully");

        } catch (Exception e) {
            log.error("ForgotPwd error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }

    // ── Verify OTP ────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public Map<String, Object> verifyOtp(
            @RequestBody Map<String, String> data) {

        String email = data.get("email");
        String otp   = data.get("otp");
        Map<String, Object> response = new HashMap<>();

        if (email == null || otp == null) {
            response.put("success", false);
            response.put("message", "Email and OTP required");
            return response;
        }

        try {
            HrUser user = findUser(email.trim());
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            PasswordResetOtp otpEntity = otpRepository
                    .findTopByEmailOrderByCreatedAtDesc(
                            user.getEmail())
                    .orElse(null);

            if (otpEntity == null) {
                response.put("success", false);
                response.put("message",
                        "No OTP found. Request again.");
                return response;
            }
            if (otpEntity.isUsed()) {
                response.put("success", false);
                response.put("message", "OTP already used.");
                return response;
            }
            if (LocalDateTime.now().isAfter(
                    otpEntity.getExpiresAt())) {
                response.put("success", false);
                response.put("message",
                        "OTP expired. Request again.");
                return response;
            }
            if (!otpEntity.getOtp().equals(otp.trim())) {
                response.put("success", false);
                response.put("message", "Invalid OTP.");
                return response;
            }

            otpEntity.setUsed(true);
            otpRepository.save(otpEntity);

            response.put("success", true);
            response.put("email",   user.getEmail());
            response.put("message", "OTP verified");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Reset Password ────────────────────────────────────────
    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(
            @RequestBody Map<String, String> data) {

        String email       = data.get("email");
        String newPassword = data.get("newPassword");
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.isBlank()) {
            response.put("success", false);
            response.put("message", "Email required");
            return response;
        }
        if (newPassword == null || newPassword.length() < 6) {
            response.put("success", false);
            response.put("message",
                    "Password must be 6+ characters");
            return response;
        }

        try {
            HrUser user = findUser(email.trim());

            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            user.setPassword(
                    passwordEncoder.encode(newPassword));
            hrUserRepository.save(user);
            otpRepository.deleteByEmail(user.getEmail());

            log.info("Password reset: {}", user.getEmail());
            response.put("success", true);
            response.put("message",
                    "Password reset successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }

    // ── Get Current User ──────────────────────────────────────
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(
            @RequestHeader(value = "Authorization",
                    required = false) String auth) {

        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || auth.isBlank()) {
                response.put("success", false);
                response.put("message", "Not authenticated");
                return response;
            }

            String token = auth.replace("Bearer ", "").trim();
            String email = jwtUtil.extractEmail(token);
            HrUser user  = findUser(email);

            if (user != null) {
                response.put("success", true);
                response.put("user", Map.of(
                        "email", user.getEmail(),
                        "name",  user.getName(),
                        "role",  user.getRole()
                ));
            } else {
                response.put("success", false);
                response.put("message", "User not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ── Helpers ───────────────────────────────────────────────
    private HrUser findUser(String email) {
        if (email == null) return null;
        String t = email.trim();
        HrUser u = hrUserRepository.findByEmail(t).orElse(null);
        if (u == null)
            u = hrUserRepository.findByEmail(
                    t.toLowerCase()).orElse(null);
        if (u == null)
            u = hrUserRepository.findByEmailIgnoreCase(t)
                    .orElse(null);
        return u;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return email;
        String[] p = email.split("@");
        String n   = p[0];
        String d   = p[1];
        if (n.length() <= 3) return "***@" + d;
        return n.substring(0, 3) + "***@" + d;
    }
}