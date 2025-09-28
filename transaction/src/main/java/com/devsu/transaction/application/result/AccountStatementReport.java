package com.devsu.transaction.application.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AccountStatementReport(
        Client client,
        Instant from,
        Instant to,
        List<AccountItem> accounts
) {
    public record Client(
            String firstName,
            String lastName,
            String identificationType,
            String identificationNumber,
            boolean active
    ) {}

    public record AccountItem(
            String accountNumber,
            String accountType,
            BigDecimal initialBalance,
            BigDecimal currentBalance,
            boolean active,
            List<MovementItem> movements
    ) {}

    public record MovementItem(
            Instant happenedAt,
            String type,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {}
}
