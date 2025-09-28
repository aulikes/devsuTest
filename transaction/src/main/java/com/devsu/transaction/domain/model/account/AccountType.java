package com.devsu.transaction.domain.model.account;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Enum de dominio para el tipo de cuenta.
 * - Mantiene un código canónico igual al catálogo ("Ahorro" / "Corriente").
 * - Expone fromCode(...) para resolver sin switches ni strings quemados en mappers.
 */
public enum AccountType {
    AHORROS("Ahorro"),
    CORRIENTE("Corriente");

    private final String code;

    AccountType(String code) { this.code = code; }

    /** Se retorna el código canónico usado por el catálogo/persistencia. */
    public String code() { return code; }

    // Índice inmutable por código (case-insensitive/trim)
    private static final Map<String, AccountType> BY_CODE =
            Stream.of(values()).collect(toUnmodifiableMap(
                    t -> t.code.toLowerCase(), t -> t
            ));

    /** Se resuelve el enum desde el código del catálogo; lanza si no existe. */
    public static AccountType fromCode(String code) {
        Objects.requireNonNull(code, "code");
        AccountType t = BY_CODE.get(code.trim().toLowerCase());
        if (t == null) throw new IllegalArgumentException("AccountType no soportado: " + code);
        return t;
    }
}
