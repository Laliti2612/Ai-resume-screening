package com.hrtech.resumescreening.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    private String secret = "secretKey123";

    private long expiration = 86400000;

    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }
}