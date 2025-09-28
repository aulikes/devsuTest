package com.devsu.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad JPA para la tabla de catálogo 'genders'.
 */
@Entity
@Data
@Table(name = "genders")
public class GenderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 16)
    private String code;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

}
