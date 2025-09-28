package com.devsu.user.integration;

import com.devsu.user.UserApplication;
import com.devsu.user.application.port.CatalogQueryPort;
import com.devsu.user.application.port.PasswordHasher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas E2E del consumo de los endpoints GET de ClientsController:
 * - GET /clientes/{id}
 * - GET /clientes/clientId/{clientId}
 *
 * Se levanta el contexto completo con PostgreSQL efímero (Testcontainers).
 * Se mockean únicamente puertos externos (PasswordHasher y CatalogQueryPort).
 * La clase queda excluida por defecto en pipelines sin Docker mediante @Tag("e2e")
 * y se habilita explícitamente con RUN_E2E=true.
 */
@Tag("e2e")
@EnabledIfEnvironmentVariable(named = "RUN_E2E", matches = "true")
@SpringBootTest(classes = UserApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Testcontainers
class ClientsGetE2EPostgresIT {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    @MockitoBean private PasswordHasher passwordHasher;
    @MockitoBean private CatalogQueryPort catalogQuery;

    private static final String BASE = "/clientes";
    private static final AtomicLong SEQ = new AtomicLong(2_000_000_000L); // números de identificación únicos

    @BeforeEach
    void setup() {
        when(passwordHasher.hash(anyString())).thenAnswer(inv -> "hash(" + inv.getArgument(0) + ")");
        when(catalogQuery.genderExists(anyString())).thenReturn(true);
        when(catalogQuery.identificationTypeExists(anyString())).thenReturn(true);
    }

    private record IdAndClientId(long id, String clientId) {}

    private String jsonCreate(Map<String, Object> overrides) throws Exception {
        var base = mapper.createObjectNode();
        base.put("firstName", "Ana");
        base.put("lastName", "Gómez");
        base.put("gender", "FEMALE");
        base.put("birthDate", "2000-01-01");
        base.put("identificationType", "CC");
        base.put("identificationNumber", String.valueOf(SEQ.getAndIncrement()));
        base.put("address", "Calle 1 # 2-3");
        base.put("phone", "3001234567");
        base.put("password", "superSecret!");
        if (overrides != null) {
            overrides.forEach((k, v) -> {
                if (v == null) base.putNull(k);
                else base.putPOJO(k, v);
            });
        }
        return mapper.writeValueAsString(base);
    }

    private IdAndClientId crearCliente() throws Exception {
        var json = jsonCreate(null);
        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode tree = mapper.readTree(body);
        return new IdAndClientId(tree.get("id").asLong(), tree.get("clientId").asText());
    }

    @Test
    @DisplayName("GET /clientes/{id} → 200 OK devuelve el cliente")
    void getById_200() throws Exception {
        var ids = crearCliente();

        mockMvc.perform(get(BASE + "/{id}", ids.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) ids.id())))
                .andExpect(jsonPath("$.clientId", is(ids.clientId())))
                .andExpect(jsonPath("$.firstName", is("Ana")));
    }

    @Test
    @DisplayName("GET /clientes/{id} → 404 Not Found cuando no existe")
    void getById_404() throws Exception {
        mockMvc.perform(get(BASE + "/{id}", 9_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("GET /clientes/{id} → 400 Bad Request cuando el id no es numérico")
    void getById_400_typeMismatch() throws Exception {
        mockMvc.perform(get(BASE + "/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'id'")))
                .andExpect(jsonPath("$.path", is(BASE + "/abc")));
    }

    @Test
    @DisplayName("GET /clientes/clientId/{clientId} → 200 OK devuelve el cliente")
    void getByClientId_200() throws Exception {
        var ids = crearCliente();

        mockMvc.perform(get(BASE + "/clientId/{clientId}", ids.clientId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(ids.clientId())))
                .andExpect(jsonPath("$.id", is((int) ids.id())));
    }

    @Test
    @DisplayName("GET /clientes/clientId/{clientId} → 404 Not Found cuando no existe")
    void getByClientId_404() throws Exception {
        mockMvc.perform(get(BASE + "/clientId/{clientId}", "NOPE-CLIENT-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }
}
