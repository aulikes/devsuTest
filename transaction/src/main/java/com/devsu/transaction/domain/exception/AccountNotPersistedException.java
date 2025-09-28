package com.devsu.transaction.domain.exception;

/** Se lanza cuando se intenta operar con una cuenta que aún no ha sido persistida (sin ID). */
public class AccountNotPersistedException extends RuntimeException {
    public AccountNotPersistedException(String message) { super(message); }
}
