package com.devsu.transaction.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MovementItemResponse(
        Long id,
        String accountNumber,
        Instant happenedAt,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter
) {}
