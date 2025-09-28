package com.devsu.user.application.port;

/**
 * Puerto que define la operación de hashing para contraseñas.
 * La implementación concreta se provee en infraestructura.
 */
public interface PasswordHasher {
    // Se recibe una contraseña en texto y se retorna su hash
    String hash(String raw);
}
