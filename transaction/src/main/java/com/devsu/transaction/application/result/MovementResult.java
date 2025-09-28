package com.devsu.transaction.application.result;

import java.math.BigDecimal;
import java.time.Instant;

public record MovementResult(
        Long id,
        Long accountId,
        Instant happenedAt,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String movementId
) {}
