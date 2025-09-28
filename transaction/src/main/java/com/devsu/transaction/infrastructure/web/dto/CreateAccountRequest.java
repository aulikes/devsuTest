package com.devsu.transaction.infrastructure.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO de entrada para crear cuenta. Contiene validaciones de contrato.
 */
public record CreateAccountRequest(
        @NotBlank(message = "Tipo de cuenta es obligatorio")
        @Pattern(
                regexp = "AHORROS|CORRIENTE",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Tipo de cuenta no válido. Valores: AHORROS o CORRIENTE"
        )
        String accountType,

        @NotNull(message = "clientId es obligatorio")
        @NotBlank(message = "clientId es obligatorio")
        String clientId,

        @NotNull(message = "Balance inicial es obligatorio")
        @DecimalMin(value = "0.00", inclusive = true, message = "Balance inicial debe ser >= 0")
        BigDecimal initialBalance
) {}
