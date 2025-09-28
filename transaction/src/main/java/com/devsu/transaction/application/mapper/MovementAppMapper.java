package com.devsu.transaction.application.mapper;

import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.Movement;

import java.math.BigDecimal;

/**
 * Mapeos entre capa de aplicación y dominio para movimientos.
 */
public final class MovementAppMapper {

    private MovementAppMapper() {}

    public static MovementResult toResult(Account account, Movement m) {
        BigDecimal amt = new BigDecimal(m.getAmount().toString());
        BigDecimal after = new BigDecimal(m.getBalanceAfter().toString());
        return new MovementResult(
                m.getId(),
                account.getId(),          // el Movement de dominio no expone accountId; se usa el de la cuenta
                m.getHappenedAt(),
                amt,
                after,
                m.getUuid()
        );
    }
}
