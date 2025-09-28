package com.devsu.user.domain.person;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad base para personas.
 * Esta clase concentra atributos comunes y sus invariantes.
 */
public abstract class Person {
    private final Long idPersona;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate birthDate;
    private IdentificationType identificationType;
    private String identificationNumber;
    private String address;
    private String phone;

    // Se construye la persona validando invariantes mínimas
    protected Person(
            Long idPersona,
            String firstName,
            String lastName,
            Gender gender,
            LocalDate birthDate,
            IdentificationType identificationType,
            String identificationNumber,
            String address,
            String phone) {
        this.idPersona = idPersona;
        setFirstName(firstName);
        setLastName(lastName);
        setGender(gender);
        setBirthDate(birthDate);
        setIdentificationType(identificationType);
        setIdentificationNumber(identificationNumber);
        setAddress(address);
        setPhone(phone);
    }

    // Se expone el ID como inmutable
    public Long getIdPersona() { return idPersona; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Gender getGender() { return gender; }
    public IdentificationType getIdentificationType() { return identificationType; }
    public String getIdentificationNumber() { return identificationNumber; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public LocalDate getBirthDate() {
        return birthDate;
    }

    // Se valida y asigna el apellido
    public void setLastName(String lastName) {
        this.lastName = Objects.requireNonNull(lastName, "lastName").trim();
    }

    // Se valida y asigna el nombre
    public void setFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(firstName, "firstName").trim();
    }

    // Se valida y asigna el género
    public void setGender(Gender gender) {
        this.gender = Objects.requireNonNull(gender, "gender");
    }
    // Se valida y asigna el tipo de identificación
    public void setIdentificationType(IdentificationType type) {
        this.identificationType = Objects.requireNonNull(type, "identificationType");
    }
    // Se valida y asigna el número de identificación
    public void setIdentificationNumber(String number) {
        this.identificationNumber = Objects.requireNonNull(number, "identificationNumber").trim();
    }
    // Se valida y asigna la dirección
    public void setAddress(String address) {
        this.address = Objects.requireNonNull(address, "address").trim();
    }
    // Se valida y asigna el teléfono
    public void setPhone(String phone) {
        this.phone = Objects.requireNonNull(phone, "phone").trim();
    }
    // Se valida y asigna la fecha de nacimiento
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = Objects.requireNonNull(birthDate, "birthDate");
    }
}
