package com.devsu.transaction.application.exception;

/** Se lanza cuando se intenta operar con una cuenta que aún no ha sido persistida (sin ID). */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) { super(message); }
}
