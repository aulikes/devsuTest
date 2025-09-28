package com.devsu.transaction.application.result;

import java.math.BigDecimal;

public record AccountResult(
        Long id,
        String accountNumber,
        String accountType,
        String clientId,
        BigDecimal currentBalance,
        BigDecimal initialBalance,
        boolean active
) {}
