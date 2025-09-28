package com.devsu.transaction.integration;

import com.devsu.transaction.TransactionApplication;
import com.devsu.transaction.application.dto.ClientResponse;
import com.devsu.transaction.application.port.AccountNumberGenerator;
import com.devsu.transaction.application.port.ClientQueryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E para creación de cuentas:
 * - Levanta el contexto completo (web + app + JPA + Flyway) con PostgreSQL efímero.
 * - Mockea únicamente dependencias externas: ClientQueryPort (User Service) y AccountNumberGenerator.
 * - Verifica respuestas HTTP, Location y payload.
 */
@SpringBootTest(classes = TransactionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("e2e")
class AccountsCreateE2EPostgresIT {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientQueryPort clientQueryPort;

    @MockitoBean
    private AccountNumberGenerator accountNumberGenerator;

    private ClientResponse stubClientOk(String clientId) {
        return new ClientResponse(
                1L,
                "John",
                "Doe",
                "MALE",
                LocalDate.of(1990, 1, 1),
                "CC",
                "1234567890",
                "Some street",
                "3000000000",
                clientId,
                true
        );
    }

    @Test
    @DisplayName("POST /cuentas → 201 Created (happy path)")
    void create_201() throws Exception {
        // Se asegura que el cliente exista en el servicio externo
        when(clientQueryPort.assertExists(anyString()))
                .thenAnswer(inv -> stubClientOk(inv.getArgument(0)));

        // Se fija el número de cuenta generado para validar Location y body
        when(accountNumberGenerator.generate()).thenReturn("ACC-E2E-0001");

        String json = """
            {
              "accountType": "AHORROS",
              "clientId": "cli-1001",
              "initialBalance": 1000.00
            }
            """;

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/cuentas/ACC-E2E-0001")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.accountNumber", is("ACC-E2E-0001")))
                .andExpect(jsonPath("$.accountType", is("AHORROS")))
                .andExpect(jsonPath("$.clientId", is("cli-1001")))
                .andExpect(jsonPath("$.initialBalance", is(1000.00)))
                .andExpect(jsonPath("$.currentBalance", is(1000.00)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("POST /cuentas → 400 Bad Request por validación (@Valid)")
    void create_400_validation() throws Exception {
        // Se simula cliente válido para aislar la validación de contrato
        when(clientQueryPort.assertExists(anyString()))
                .thenAnswer(inv -> stubClientOk(inv.getArgument(0)));

        // No importa el número de cuenta porque debe fallar antes por validación
        when(accountNumberGenerator.generate()).thenReturn("ACC-E2E-XYZ");

        // accountType inválido y initialBalance negativo
        String json = """
            {
              "accountType": "PLAZO_FIJO",
              "clientId": "",
              "initialBalance": -10
            }
            """;

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.path", is("/cuentas")))
                .andExpect(jsonPath("$.violations", not(empty())));
    }

    @Test
    @DisplayName("POST /cuentas → 404 Not Found cuando el cliente no existe (ClientQueryPort)")
    void create_404_client_not_found() throws Exception {
        // Se simula que el servicio externo no encuentra el cliente
        when(clientQueryPort.assertExists(anyString())).thenReturn(null);

        when(accountNumberGenerator.generate()).thenReturn("ACC-E2E-0002");

        String json = """
            {
              "accountType": "CORRIENTE",
              "clientId": "cli-inexistente",
              "initialBalance": 0
            }
            """;

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /cuentas → 409 Conflict por número de cuenta duplicado")
    void create_409_duplicate_account_number() throws Exception {
        // Se simula cliente válido
        when(clientQueryPort.assertExists(anyString()))
                .thenAnswer(inv -> stubClientOk(inv.getArgument(0)));

        // Primera creación y segunda con el mismo número para provocar UNIQUE
        when(accountNumberGenerator.generate()).thenReturn("ACC-E2E-0003", "ACC-E2E-0003");

        String body = """
            {
              "accountType": "AHORROS",
              "clientId": "cli-dup",
              "initialBalance": 50
            }
            """;

        // Primer alta OK
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Segundo alta con mismo número → viola uk_accounts_account_number
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }
}
