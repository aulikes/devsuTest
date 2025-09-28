package com.devsu.user.application.service;

import com.devsu.user.application.command.UpdateClientCommand;
import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.exception.DuplicateIdentificationException;
import com.devsu.user.application.exception.InvalidCatalogCodeException;
import com.devsu.user.application.port.CatalogQueryPort;
import com.devsu.user.application.port.PasswordHasher;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.client.ClientRepository;
import com.devsu.user.domain.person.Gender;
import com.devsu.user.domain.person.IdentificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Pruebas para UpdateClientService basadas en el código actual del proyecto.
 * - Firma real: ClientResult execute(UpdateClientCommand command)
 * - Valida catálogos (CatalogQueryPort)
 * - Valida unicidad solo si cambia identificationNumber
 * - Rehashea password solo si no es null ni en blanco
 */
@ExtendWith(MockitoExtension.class)
class UpdateClientServiceTest {

    @Mock private ClientRepository repository;
    @Mock private CatalogQueryPort catalogQuery;
    @Mock private PasswordHasher passwordHasher;

    @InjectMocks
    private UpdateClientService service;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    // Utilidad: construir agregado coherente con tu dominio
    private Client persistedClient(Long id, boolean active, String idType, String idNumber, String passwordHash) {
        return Client.fromPersistence(
                id,
                200L,
                "Ana",
                "Gómez",
                Gender.FEMALE,
                LocalDate.parse("2000-01-01"),
                IdentificationType.valueOf(idType),
                idNumber,
                "Calle 1 # 2-3",
                "3001234567",
                "ANA-001",
                passwordHash,
                active
        );
    }

    private UpdateClientCommand cmd(Long id, String firstName, String lastName,
                                    String gender, LocalDate birthDate,
                                    String idType, String idNumber,
                                    String address, String phone,
                                    String password) {
        return new UpdateClientCommand(id, firstName, lastName, gender, birthDate,
                idType, idNumber, address, phone, password);
    }

    @Test
    @DisplayName("Actualiza sin re-hashear cuando el password es null")
    void update_withoutRehash_whenPasswordNull() {
        var existing = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("CC")).thenReturn(true);

        var saved = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.save(any(Client.class))).thenReturn(saved);

        var command = cmd(10L, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"), "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000", null);

        ClientResult result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);

        verify(passwordHasher, never()).hash(anyString());
        verify(repository, never()).existsByIdentification(anyString());

        verify(repository).save(clientCaptor.capture());
        var toPersist = clientCaptor.getValue();
        assertThat(toPersist.getPassword()).isEqualTo("{bcrypt}OLD");
        assertThat(toPersist.getFirstName()).isEqualTo("Ana María");
        assertThat(toPersist.getLastName()).isEqualTo("Gómez R.");
        assertThat(toPersist.getAddress()).isEqualTo("Calle 10 # 20-30");
        assertThat(toPersist.getPhone()).isEqualTo("3015550000");

        verify(repository).findById(10L);
        verify(catalogQuery).genderExists("FEMALE");
        verify(catalogQuery).identificationTypeExists("CC");
        verifyNoMoreInteractions(repository, catalogQuery, passwordHasher);
    }

    @Test
    @DisplayName("Actualiza sin re-hashear cuando el password es en blanco")
    void update_withoutRehash_whenPasswordBlank() {
        var existing = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("CC")).thenReturn(true);

        var saved = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.save(any(Client.class))).thenReturn(saved);

        var command = cmd(10L, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"), "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000", "   ");

        ClientResult result = service.execute(command);

        assertThat(result).isNotNull();
        verify(passwordHasher, never()).hash(anyString());
        verify(repository, never()).existsByIdentification(anyString());

        verify(repository).save(clientCaptor.capture());
        assertThat(clientCaptor.getValue().getPassword()).isEqualTo("{bcrypt}OLD");

        verify(repository).findById(10L);
        verify(catalogQuery).genderExists("FEMALE");
        verify(catalogQuery).identificationTypeExists("CC");
        verifyNoMoreInteractions(repository, catalogQuery, passwordHasher);
    }

    @Test
    @DisplayName("Actualiza hasheando cuando se envía un nuevo password")
    void update_withRehash_whenNewPassword() {
        var existing = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("CC")).thenReturn(true);
        when(passwordHasher.hash("newPass!")).thenReturn("{bcrypt}NEW");

        var saved = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}NEW");
        when(repository.save(any(Client.class))).thenReturn(saved);

        var command = cmd(10L, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"), "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000", "newPass!");

        ClientResult result = service.execute(command);

        assertThat(result).isNotNull();
        verify(passwordHasher).hash("newPass!");
        verify(repository, never()).existsByIdentification(anyString());

        verify(repository).save(clientCaptor.capture());
        assertThat(clientCaptor.getValue().getPassword()).isEqualTo("{bcrypt}NEW");

        verify(repository).findById(10L);
        verify(catalogQuery).genderExists("FEMALE");
        verify(catalogQuery).identificationTypeExists("CC");
        verifyNoMoreInteractions(repository, catalogQuery, passwordHasher);
    }

    @Test
    @DisplayName("Falla con DuplicateIdentificationException si se cambia a una identificación ya existente")
    void fail_whenChangingIdentificationToExisting() {
        var existing = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("CC")).thenReturn(true);

        when(repository.existsByIdentification("9999999999")).thenReturn(true);

        var command = cmd(10L, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"), "CC", "9999999999",
                "Calle 10 # 20-30", "3015550000", null);

        assertThrows(DuplicateIdentificationException.class, () -> service.execute(command));

        verify(repository).findById(10L);
        verify(repository).existsByIdentification("9999999999");
        verify(repository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
        verifyNoMoreInteractions(repository, catalogQuery, passwordHasher);
    }

    @Test
    @DisplayName("Falla con ClientNotFoundException cuando el cliente no existe")
    void fail_whenClientNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        var command = cmd(99L, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"), "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000", null);

        assertThrows(ClientNotFoundException.class, () -> service.execute(command));

        verify(repository).findById(99L);
        verify(repository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
        verifyNoMoreInteractions(repository, catalogQuery, passwordHasher);
    }

    @Test
    @DisplayName("Falla con InvalidCatalogCodeException cuando el género es inválido")
    void fail_whenGenderInvalid() {
        var existing = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(catalogQuery.genderExists("BAD")).thenReturn(false);

        var command = cmd(10L, "Ana María", "Gómez R.", "BAD",
                LocalDate.parse("2000-02-02"), "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000", null);

        assertThrows(InvalidCatalogCodeException.class, () -> service.execute(command));

        verify(catalogQuery).genderExists("BAD");
        verify(catalogQuery, never()).identificationTypeExists(anyString());
        verify(repository, never()).existsByIdentification(anyString());
        verify(repository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
        verifyNoMoreInteractions(repository, catalogQuery, passwordHasher);
    }

    @Test
    @DisplayName("Falla con InvalidCatalogCodeException cuando el tipo de identificación es inválido")
    void fail_whenIdentificationTypeInvalid() {
        var existing = persistedClient(10L, true, "CC", "1234567890", "{bcrypt}OLD");
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("BAD")).thenReturn(false);

        var command = cmd(10L, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"), "BAD", "1234567890",
                "Calle 10 # 20-30", "3015550000", null);

        assertThrows(InvalidCatalogCodeException.class, () -> service.execute(command));

        verify(catalogQuery).genderExists("FEMALE");
        verify(catalogQuery).identificationTypeExists("BAD");
        verify(repository, never()).existsByIdentification(anyString());
        verify(repository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
        verifyNoMoreInteractions(repository, catalogQuery, passwordHasher);
    }
}
