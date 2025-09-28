package com.devsu.user.application.service;

import com.devsu.user.application.exception.ClientNotFoundException;
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
 * Este test valida el caso de uso de eliminación de clientes.
 * - Si el cliente existe y está activo, debe desactivarlo (borrado lógico) y persistir el cambio.
 * - Si el cliente ya está inactivo, la operación es idempotente y no debe persistir nada.
 * - Si no existe, debe lanzar ClientNotFoundException.
 */
@ExtendWith(MockitoExtension.class)
class DeleteClientServiceTest {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private DeleteClientService service;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    // Utilidad para crear un agregado Client "desde persistencia" (coincide con tu patrón)
    private Client persistedClient(Long id, boolean active) {
        return Client.fromPersistence(
                id,
                200L,                      // id de person
                "Ana",
                "Gómez",
                Gender.FEMALE,
                LocalDate.parse("2000-01-01"),
                IdentificationType.CC,
                "1234567890",
                "Calle 1 # 2-3",
                "3001234567",
                "ANA-001",
                "{bcrypt}HASHED",
                active
        );
    }

    @Test
    @DisplayName("Debe desactivar y persistir cuando el cliente existe y está activo")
    void shouldDeactivateAndPersist_WhenClientExistsAndIsActive() {
        // --- Dado: cliente activo existente ---
        var existing = persistedClient(10L, true);
        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        // --- Y: el repositorio devuelve la entidad ya desactivada tras guardar ---
        var saved = persistedClient(10L, false);
        when(repository.save(any(Client.class))).thenReturn(saved);

        // --- Cuando: se ejecuta el caso de uso ---
        // Firma esperada del servicio: void execute(Long id)  (controlador retorna 204 No Content)
        service.execute(10L);

        // --- Entonces: se buscó por ID ---
        verify(repository, times(1)).findById(10L);

        // --- Y: se guardó el agregado con estado inactivo ---
        verify(repository, times(1)).save(clientCaptor.capture());
        var toPersist = clientCaptor.getValue();

        // Se valida que el agregado que se persiste queda inactivo
        assertThat(toPersist).isNotNull();
        assertThat(toPersist.isActive()).isFalse();

        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Debe ser idempotente: si ya está inactivo, no persiste cambios")
    void shouldBeIdempotent_WhenClientIsAlreadyInactive() {
        // --- Dado: cliente ya inactivo ---
        var existing = persistedClient(11L, false);
        when(repository.findById(11L)).thenReturn(Optional.of(existing));

        // --- Cuando: se ejecuta el caso de uso ---
        service.execute(11L);

        // --- Entonces: se buscó por ID pero no se llamó a save ---
        verify(repository, times(1)).findById(11L);
        verify(repository, never()).save(any(Client.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Debe lanzar ClientNotFoundException si el cliente no existe")
    void shouldThrow_WhenClientNotFound() {
        // --- Dado: no existe el cliente ---
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // --- Cuando/Entonces: se espera excepción ---
        assertThrows(ClientNotFoundException.class, () -> service.execute(99L));

        // --- Y: no se debe intentar guardar nada ---
        verify(repository, times(1)).findById(99L);
        verify(repository, never()).save(any(Client.class));
        verifyNoMoreInteractions(repository);
    }
}
