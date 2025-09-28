package com.devsu.transaction.application.mapper;

import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.domain.model.account.Account;

import java.math.BigDecimal;

/**
 * Mapeos entre capa de aplicación y dominio para cuentas.
 * No contiene lógica de negocio.
 */
public final class AccountAppMapper {

    private AccountAppMapper() {}

    public static AccountResult toResult(Account a) {
        BigDecimal currentBalance = new BigDecimal(a.getCurrentBalance().toString());
        BigDecimal initialBalance = new BigDecimal(a.getInitialBalance().toString());
        return new AccountResult(
                a.getId(),
                a.getAccountNumber(),
                a.getType().name(),
                a.getClientId(),
                currentBalance,
                initialBalance,
                a.isActive()
        );
    }
}
