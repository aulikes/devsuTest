package com.devsu.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "persons")
public class PersonJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstName", nullable = false, length = 120)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 120)
    private String lastName;

    @Column(name = "birthDate", nullable = false)
    private LocalDate birthDate;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Column(name = "phone", nullable = false, length = 40)
    private String phone;

    @Column(name = "identification_number", nullable = false, length = 40)
    private String identificationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "gender_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_persons_gender")
    )
    private GenderJpaEntity gender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "identification_type_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_persons_ident_type")
    )
    private IdentificationTypeJpaEntity identificationType;
}
