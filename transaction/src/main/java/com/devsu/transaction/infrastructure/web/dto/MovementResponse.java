package com.devsu.transaction.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de salida para un movimiento ya registrado.
 */
public record MovementResponse(
        Long id,
        Long accountId,
        Instant happenedAt,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String movementId
) {}
