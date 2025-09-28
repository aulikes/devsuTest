package com.devsu.transaction.domain.exception;

/** Se lanza cuando un monto no cumple las reglas del dominio (<= 0, negativo, etc.). */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) { super(message); }
}
