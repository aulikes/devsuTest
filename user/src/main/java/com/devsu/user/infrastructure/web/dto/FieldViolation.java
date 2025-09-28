package com.devsu.user.infrastructure.web.dto;

/**
 * DTO que representa una violación de validación a nivel de campo.
 */
public record FieldViolation(
        // Se almacena el nombre del campo con error
        String field,
        // Se almacena el mensaje de validación asociado
        String message
) {}
