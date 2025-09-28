package com.devsu.user.application.result;

import java.time.LocalDate;

public record ClientResult(
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
