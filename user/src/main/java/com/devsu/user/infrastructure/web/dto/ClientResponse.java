package com.devsu.user.infrastructure.web.dto;

import java.time.LocalDate;

/**
 * DTO de salida para exponer datos de cliente por HTTP.
 */
public record ClientResponse(
        Long id,
        String firstName,
        String lastName,
        String gender,
        LocalDate birthDate,
        String identificationType,
        String identificationNumber,
        String address,
        String phone,
        String clientId,
        boolean status
) {}
