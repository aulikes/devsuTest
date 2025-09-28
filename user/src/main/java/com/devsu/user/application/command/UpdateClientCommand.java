package com.devsu.user.application.command;

import java.time.LocalDate;

/**
 * Record que representa el comando para actualizar un cliente existente.
 */
public record UpdateClientCommand(
        Long id,
        String firstName,
        String lastName,
        String gender,
        LocalDate birthDate,
        String identificationType,
        String identificationNumber,
        String address,
        String phone,
        String password
) {}
