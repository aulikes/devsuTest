package com.devsu.transaction.domain.exception;

/** Se lanza cuando se intenta operar sobre una cuenta inactiva. */
public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(String message) { super(message); }
}
