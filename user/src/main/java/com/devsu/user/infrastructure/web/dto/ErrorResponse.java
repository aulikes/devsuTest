package com.devsu.user.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO que representa una respuesta de error estándar para la API.
 */
public record ErrorResponse(
        // Se almacena el instante en que ocurre el error (UTC)
        Instant timestamp,
        // Se almacena el código HTTP de la respuesta (ej. 400, 409, 500)
        int status,
        // Se almacena el nombre estándar del estado HTTP (ej. "BAD_REQUEST")
        String error,
        // Se almacena un mensaje legible con la causa principal
        String message,
        // Se almacena la ruta del request que generó el error
        String path,
        // Se almacenan detalles de errores de campo cuando aplica
        List<FieldViolation> violations
) {}
