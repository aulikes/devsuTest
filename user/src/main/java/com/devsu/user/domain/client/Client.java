package com.devsu.user.domain.client;

import com.devsu.user.domain.person.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Entidad Cliente que extiende Person y agrega atributos propios.
 */
public class Client extends Person {

    private final Long id;
    private final String clientId;
    private String password;
    private boolean active;

    private Client(
            Long id, Long idPersona, String firstName, String lastName,
            Gender gender, LocalDate birthDate, IdentificationType identificationType,
            String identificationNumber, String address, String phone,
            String clientId, String password, boolean active) {
        super(idPersona, firstName, lastName, gender, birthDate, identificationType, identificationNumber, address, phone);

        this.id = id;
        this.clientId = Objects.requireNonNull(clientId, "clientId").trim();
        this.active = active;
        setPassword(password);
    }

    // ===== Fábricas =====
    //Client Nuevo
    public static Client create(String firstName, String lastName,
                                Gender gender, LocalDate birthDate, IdentificationType identificationType,
                                String identificationNumber, String address, String phone,
                                String clientId, String password) {
        return new Client(null, null, firstName, lastName,
                gender, birthDate, identificationType,
                identificationNumber, address, phone,
                clientId, password, true);
    }

    //Client obtenido desde BD
    public static Client fromPersistence(Long id, Long idPersona, String firstName, String lastName,
                                         Gender gender, LocalDate birthDate, IdentificationType identificationType,
                                         String identificationNumber, String address, String phone,
                                         String clientId, String password, boolean active) {
        if (id == null) throw new IllegalArgumentException("id is required");
        if (idPersona == null) throw new IllegalArgumentException("idPersona is required");
        return new Client(id, idPersona, firstName, lastName,
                gender, birthDate, identificationType,
                identificationNumber, address, phone,
                clientId, password, active);
    }

    public Long getId() { return id; }
    public String getClientId() { return clientId; }
    public String getPassword() { return password; }
    public boolean isActive() { return active; }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("password is required");
        this.password = password;
    }

    // Se permite activar/desactivar el cliente
    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }
}
