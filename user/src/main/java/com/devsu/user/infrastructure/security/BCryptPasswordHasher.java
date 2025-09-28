package com.devsu.user.infrastructure.security;

import com.devsu.user.application.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementación de PasswordHasher usando BCrypt.
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    // Se delega el hashing a BCryptPasswordEncoder
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String raw) {
        return encoder.encode(raw);
    }
}
