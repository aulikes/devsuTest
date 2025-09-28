package com.devsu.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA para la tabla de catálogo 'identification_types'.
 */
@Entity
@Data
@Table(name = "identification_types")
public class IdentificationTypeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se almacena el código único del tipo (ej: CC)
    @Column(name = "code", nullable = false, length = 16)
    private String code;

    // Se almacena el nombre descriptivo (ej: "Cédula de ciudadanía")
    @Column(name = "name", nullable = false, length = 64)
    private String name;
}

