package com.devsu.user.application.exception;

/**
 * Excepción de aplicación que indica que no existe un cliente con el identificador solicitado.
 */
public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String message) { super(message); }
}
