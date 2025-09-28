package com.devsu.transaction.infrastructure.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO de entrada para registrar un movimiento.
 * Nota: el signo del monto define la intención:
 *  - monto > 0  => depósito
 *  - monto < 0  => retiro
 * La validación semántica vive en el dominio.
 */
public record CreateMovementRequest(
        @NotBlank(message = "accountNumber es obligatorio")
        @Size(max = 32, message = "accountNumber supera la longitud permitida")
        String accountNumber,

        @NotNull(message = "amount es obligatorio")
        @Digits(integer = 17, fraction = 2, message = "amount debe tener hasta 2 decimales")
        BigDecimal amount
) {
        @AssertTrue(message = "amount no puede ser 0")
        public boolean isAmountNonZero() {
                return amount != null && amount.compareTo(BigDecimal.ZERO) != 0;
        }
}
