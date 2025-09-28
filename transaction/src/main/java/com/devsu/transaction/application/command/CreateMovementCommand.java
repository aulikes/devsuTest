package com.devsu.transaction.application.command;

import java.math.BigDecimal;

public record CreateMovementCommand(
    String accountNumber,
    BigDecimal amount
) {}
