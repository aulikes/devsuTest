package com.devsu.transaction.infrastructure.web.dto;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        String accountType,
        String clientId,
        BigDecimal currentBalance,
        BigDecimal initialBalance,
        boolean active
) {}
