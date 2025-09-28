package com.devsu.transaction.domain.repository;

import com.devsu.transaction.domain.model.account.Movement;

import java.time.Instant;
import java.util.List;

/**
 * Puerto de dominio para persistencia y consulta de movimientos.
 */
public interface MovementRepository {
    // Se persiste un movimiento asociado a una cuenta
    Movement save(Movement movement);
    // Se listan movimientos por cuenta y rango de fechas
    List<Movement> findByAccountAndDateRange(Long accountId, Instant from, Instant to);
}
