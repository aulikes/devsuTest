package com.devsu.user.application.exception;

/**
 * Excepción de aplicación que indica que un código de catálogo no es válido.
 */
public class InvalidCatalogCodeException extends RuntimeException {
    public InvalidCatalogCodeException(String message) { super(message); }
}
