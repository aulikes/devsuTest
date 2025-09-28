package com.devsu.transaction.application.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String id) {
        super("Client not found: " + id);
    }
}
