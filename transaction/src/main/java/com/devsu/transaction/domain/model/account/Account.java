package com.devsu.transaction.domain.model.account;

import com.devsu.transaction.domain.exception.AccountNotPersistedException;
import com.devsu.transaction.domain.exception.InactiveAccountException;
import com.devsu.transaction.domain.exception.InsufficientFundsException;
import com.devsu.transaction.domain.exception.InvalidAmountException;
import com.devsu.transaction.domain.model.money.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad Cuenta que actúa como Aggregate Root para movimientos.
 * Mantiene el saldo actual y referencia al cliente propietario.
 */
public class Account {
    private final Long id;
    private final String accountNumber;
    private final AccountType type;
    private final Money initialBalance;
    private boolean active;
    private final String clientId;
    private Money currentBalance;
    private final Instant createdAt;

    private final List<Movement> movements;

    private Account(Long id, String accountNumber, AccountType type, Money initialBalance,
                    boolean active, String clientId, Instant createdAt, List<Movement> movements, Money currentBalance) {
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.active = active;
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.createdAt = createdAt;
        this.movements = (movements == null) ? new ArrayList<>() : new ArrayList<>(movements);

        if (accountNumber == null || accountNumber.trim().isEmpty())
            throw new IllegalArgumentException("accountNumber is required");
        this.accountNumber = accountNumber.trim();

        this.initialBalance = Objects.requireNonNull(initialBalance, "initialBalance");
        if (this.initialBalance.isNegative())
            throw new InvalidAmountException("initialBalance must be >= 0");

        this.currentBalance = Objects.requireNonNull(currentBalance, "currentBalance");
        if (this.currentBalance.isNegative())
            throw new InvalidAmountException("currentBalance must be >= 0");
    }

    // ===== Fábricas =====
    //Account Nuevo
    public static Account create(String accountNumber, AccountType type,
                                 Money initialBalance, String clientId) {
        return new Account(null, accountNumber, type, initialBalance, true,
                clientId, Instant.now(), List.of(), initialBalance);
    }

    //Account obtenido desde BD
    public static Account fromPersistence(Long id, String accountNumber, AccountType type,
                                          Money initialBalance, String clientId, Instant createdAt,
                                          boolean active, List<Movement> movements, Money currentBalance) {
        if (id == null) throw new IllegalArgumentException("id is required");
        return new Account(id, accountNumber, type, initialBalance, active, clientId, createdAt, movements, currentBalance);
    }

    // ===== Lógica de Negocio =====
    /**
     * Registra movimientos
     */
    public String registerMovement(BigDecimal amount) {
        if (this.id == null)
            throw new AccountNotPersistedException("No se pueden registrar movimientos en una cuenta sin ID");
        requireActive();

        Money delta = Money.of(amount);
        if (delta.isZero()) throw new InvalidAmountException("El monto del movimiento debe ser diferente de cero");

        MovementType type;
        Money newBalance;
        if (delta.isNegative()) { // Retirar
            Money nb = this.currentBalance.add(delta);
            if (nb.isNegative()) throw new InsufficientFundsException("Fondos insuficientes");
            newBalance = nb;
            type = MovementType.WITHDRAWAL;
        } else { // Consignar
            newBalance = currentBalance.add(delta);
            type = MovementType.DEPOSIT;
        }

        this.currentBalance = newBalance;            // <-- reasignar SIEMPRE

        // Registrar movimiento con monto positivo (dominio guarda positivos)
        Movement movement = Movement.create(type, delta, newBalance);
        this.movements.add(movement);
        return movement.getUuid();
    }

    private void requireActive() {
        if (!this.active) throw new InactiveAccountException("La cuenta está inactiva");
    }

    // Se permite activar/desactivar la cuenta
    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }


    // ===== Getters =====
    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public AccountType getType() { return type; }
    public Money getInitialBalance() { return initialBalance; }
    public boolean isActive() { return active; }
    public String getClientId() { return clientId; }
    public Money getCurrentBalance() { return currentBalance; }
    public Instant getCreatedAt() { return createdAt; }
    //Se expone una vista inmutable del historial para lectura.
    public List<Movement> getMovements() {
        return Collections.unmodifiableList(movements);
    }

}
