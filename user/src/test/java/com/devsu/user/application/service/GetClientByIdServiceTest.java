package com.devsu.user.application.service;

import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.client.ClientRepository;
import com.devsu.user.domain.person.Gender;
import com.devsu.user.domain.person.IdentificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Este test valida el caso de uso de consulta por ID.
 * - Si el cliente existe, debe retornar un ClientResult correctamente mapeado.
 * - Si no existe, debe lanzar ClientNotFoundException.
 */
@ExtendWith(MockitoExtension.class)
class GetClientByIdServiceTest {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private GetClientByIdService service;

    // Utilidad para construir un agregado Client "desde persistencia" con datos consistentes
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
    @DisplayName("Debe retornar ClientResult cuando el cliente existe")
    void shouldReturnResult_WhenClientExists() {
        // --- Dado: repositorio retorna el agregado existente ---
        var client = persistedClient(10L, true);
        when(repository.findById(10L)).thenReturn(Optional.of(client));

        // --- Cuando: se ejecuta el caso de uso ---
        ClientResult result = service.execute(10L);

        // --- Entonces: el resultado refleja el agregado ---
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.clientId()).isEqualTo("ANA-001");
        assertThat(result.firstName()).isEqualTo("Ana");
        assertThat(result.lastName()).isEqualTo("Gómez");
        assertThat(result.gender()).isEqualTo("FEMALE");
        assertThat(result.identificationType()).isEqualTo("CC");
        assertThat(result.identificationNumber()).isEqualTo("1234567890");
        assertThat(result.address()).isEqualTo("Calle 1 # 2-3");
        assertThat(result.phone()).isEqualTo("3001234567");
        assertThat(result.status()).isTrue();

        // --- Y: se verifican interacciones mínimas ---
        verify(repository, times(1)).findById(10L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Debe lanzar ClientNotFoundException si el cliente no existe")
    void shouldThrow_WhenClientNotFound() {
        // --- Dado: repositorio no encuentra el cliente ---
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // --- Cuando/Entonces: se espera excepción ---
        assertThrows(ClientNotFoundException.class, () -> service.execute(99L));

        // --- Y: no hay más interacciones ---
        verify(repository, times(1)).findById(99L);
        verifyNoMoreInteractions(repository);
    }
}
