package com.devsu.user.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO de entrada para actualizar un cliente.
 * Esta clase permite actualización completa; la contraseña es opcional.
 */
public record UpdateClientRequest(
        @NotBlank @Size(max = 120)
        String firstName,

        @NotBlank @Size(max = 120)
        String lastName,

        @NotBlank
        String gender,

        @Past
        @Schema(example = "1995-03-21")
        LocalDate birthDate,

        @NotBlank
        String identificationType,

        @NotBlank @Size(max = 40)
        String identificationNumber,

        @NotBlank @Size(max = 200)
        String address,

        @NotBlank @Size(max = 40)
        String phone,

        @Size(min = 6, max = 100)
        String password // opcional: si viene null o blank, no se cambia

) {}
