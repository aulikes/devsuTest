package com.devsu.transaction.application.exception;

/** Se lanza cuando se intenta operar con una cuenta que aún no ha sido persistida (sin ID). */
public class MovementNotFoundException extends RuntimeException {
    public MovementNotFoundException(String message) { super(message); }
}
