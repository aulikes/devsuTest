package com.devsu.user.application.service;

import com.devsu.user.application.command.CreateClientCommand;
import com.devsu.user.application.exception.DuplicateIdentificationException;
import com.devsu.user.application.exception.InvalidCatalogCodeException;
import com.devsu.user.application.port.AccountNumberGenerator;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Este test se basa en el código real del proyecto.
 * - Verifica validación de catálogos vía CatalogQueryPort.
 * - Verifica unicidad por identificación vía ClientRepository.existsByIdentification(...).
 * - Verifica generación de clientId vía AccountNumberGenerator.
 * - Verifica hashing de la contraseña vía PasswordHasher.
 * - Verifica mapeo a ClientResult.
 */
@ExtendWith(MockitoExtension.class)
class CreateClientServiceTest {

    @Mock private ClientRepository repository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private CatalogQueryPort catalogQuery;
    @Mock private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private CreateClientService service;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    private CreateClientCommand validCommand() {
        return new CreateClientCommand(
                "Ana",
                "Gómez",
                "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC",
                "1234567890",
                "Calle 1 # 2-3",
                "3001234567",
                "pass123"
        );
    }

    @Test
    @DisplayName("Debe crear cliente: valida catálogos, unicidad, hashea password, genera clientId y persiste")
    void shouldCreateClient_HashPassword_GenerateClientId_AndPersist() {
        // --- Dado: command válido del proyecto ---
        var cmd = validCommand();

        // --- Y: catálogos válidos ---
        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("CC")).thenReturn(true);

        // --- Y: identificación NO existe ---
        when(repository.existsByIdentification("1234567890")).thenReturn(false);

        // --- Y: se genera un clientId y se hashea el password ---
        when(accountNumberGenerator.generate()).thenReturn("ANA-001");
        when(passwordHasher.hash("pass123")).thenReturn("{bcrypt}HASHED");

        // --- Y: al guardar, la persistencia retorna un agregado con IDs asignados ---
        var saved = Client.fromPersistence(
                10L,                 // id
                20L,                 // idPersona
                "Ana",
                "Gómez",
                Gender.FEMALE,
                LocalDate.parse("2000-01-01"),
                IdentificationType.CC,
                "1234567890",
                "Calle 1 # 2-3",
                "3001234567",
                "ANA-001",           // clientId generado
                "{bcrypt}HASHED",    // password ya hasheado
                true                 // active
        );
        when(repository.save(any(Client.class))).thenReturn(saved);

        // --- Cuando: se ejecuta el caso de uso ---
        ClientResult result = service.execute(cmd);

        // --- Entonces: el resultado refleja el agregado persistido ---
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.clientId()).isEqualTo("ANA-001");
        assertThat(result.firstName()).isEqualTo("Ana");
        assertThat(result.lastName()).isEqualTo("Gómez");
        assertThat(result.gender()).isEqualTo("FEMALE");
        assertThat(result.identificationType()).isEqualTo("CC");
        assertThat(result.identificationNumber()).isEqualTo("1234567890");
        assertThat(result.status()).isTrue();

        // --- Y: se validaron catálogos ---
        verify(catalogQuery, times(1)).genderExists("FEMALE");
        verify(catalogQuery, times(1)).identificationTypeExists("CC");

        // --- Y: se validó unicidad por identificación ---
        verify(repository, times(1)).existsByIdentification("1234567890");

        // --- Y: se generó clientId y se hasheó el password ---
        verify(accountNumberGenerator, times(1)).generate();
        verify(passwordHasher, times(1)).hash("pass123");

        // --- Y: se persistió con el objeto correcto (password hasheado y clientId generado) ---
        verify(repository).save(clientCaptor.capture());
        var toPersist = clientCaptor.getValue();
        assertThat(toPersist.getClientId()).isEqualTo("ANA-001");
        assertThat(toPersist.getPassword()).isEqualTo("{bcrypt}HASHED");
        assertThat(toPersist.isActive()).isTrue();

        verifyNoMoreInteractions(repository, passwordHasher, catalogQuery, accountNumberGenerator);
    }

    @Test
    @DisplayName("No debe crear cliente si el código de género es inválido")
    void shouldFail_WhenGenderCodeIsInvalid() {
        var cmd = validCommand();

        // Género inválido => se corta antes de cualquier otra interacción
        when(catalogQuery.genderExists("FEMALE")).thenReturn(false);

        assertThrows(InvalidCatalogCodeException.class, () -> service.execute(cmd));

        verify(catalogQuery, times(1)).genderExists("FEMALE");
        verify(catalogQuery, never()).identificationTypeExists(anyString());
        verify(repository, never()).existsByIdentification(anyString());
        verify(accountNumberGenerator, never()).generate();
        verify(passwordHasher, never()).hash(anyString());
        verify(repository, never()).save(any());
        verifyNoMoreInteractions(repository, passwordHasher, catalogQuery, accountNumberGenerator);
    }

    @Test
    @DisplayName("No debe crear cliente si el tipo de identificación es inválido")
    void shouldFail_WhenIdentificationTypeIsInvalid() {
        var cmd = validCommand();

        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("CC")).thenReturn(false);

        assertThrows(InvalidCatalogCodeException.class, () -> service.execute(cmd));

        verify(catalogQuery, times(1)).genderExists("FEMALE");
        verify(catalogQuery, times(1)).identificationTypeExists("CC");
        verify(repository, never()).existsByIdentification(anyString());
        verify(accountNumberGenerator, never()).generate();
        verify(passwordHasher, never()).hash(anyString());
        verify(repository, never()).save(any());
        verifyNoMoreInteractions(repository, passwordHasher, catalogQuery, accountNumberGenerator);
    }

    @Test
    @DisplayName("No debe crear cliente si la identificación ya existe")
    void shouldFail_WhenIdentificationAlreadyExists() {
        var cmd = validCommand();

        when(catalogQuery.genderExists("FEMALE")).thenReturn(true);
        when(catalogQuery.identificationTypeExists("CC")).thenReturn(true);
        when(repository.existsByIdentification("1234567890")).thenReturn(true);

        assertThrows(DuplicateIdentificationException.class, () -> service.execute(cmd));

        verify(repository, times(1)).existsByIdentification("1234567890");
        verify(accountNumberGenerator, never()).generate();
        verify(passwordHasher, never()).hash(anyString());
        verify(repository, never()).save(any());
        verifyNoMoreInteractions(repository, passwordHasher, catalogQuery, accountNumberGenerator);
    }
}
