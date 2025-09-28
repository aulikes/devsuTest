package com.devsu.transaction.domain.exception;

/** Se lanza cuando un retiro dejaría el saldo por debajo de cero. */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) { super(message); }
}
