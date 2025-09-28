package com.devsu.transaction.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

/** Request para activar/desactivar una cuenta. */
public record ChangeAccountStatusRequest(
        @NotNull(message = "El estado 'active' es obligatorio")
        Boolean active
) {}
