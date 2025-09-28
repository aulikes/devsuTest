package com.devsu.transaction.application.command;

import java.math.BigDecimal;

public record CreateAccountCommand(
        String accountType,
        String clientId,
        BigDecimal initialBalance
) {}
