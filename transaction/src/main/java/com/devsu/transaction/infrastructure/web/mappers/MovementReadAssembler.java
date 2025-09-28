package com.devsu.transaction.infrastructure.web.mappers;

import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.infrastructure.web.dto.MovementItemResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MovementReadAssembler {
    public MovementItemResponse toItem(Movement m, String accountNumber) {
        return new MovementItemResponse(
                m.getId(),
                accountNumber,
                m.getHappenedAt(),
                m.getType().name(),
                new BigDecimal(m.getAmount().toString()),
                new BigDecimal(m.getBalanceAfter().toString())
        );
    }
}
