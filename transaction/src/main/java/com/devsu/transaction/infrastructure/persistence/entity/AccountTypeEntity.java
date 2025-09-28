package com.devsu.transaction.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de tipo de cuenta (Ahorro, Corriente, etc.).
 */
@Entity
@Table(name = "account_types")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", updatable = false, nullable = false, length = 30)
    private String code;

    @Column(name = "description", nullable = false, length = 200)
    private String description;
}
