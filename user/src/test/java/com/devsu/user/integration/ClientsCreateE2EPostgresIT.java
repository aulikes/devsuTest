package com.devsu.user.integration;

import com.devsu.user.UserApplication;
import com.devsu.user.application.port.CatalogQueryPort;
import com.devsu.user.application.port.PasswordHasher;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas E2E solo del endpoint POST /clientes.
 * Se levanta el contexto completo con PostgreSQL efímero (Testcontainers).
 * Se mockean únicamente puertos externos (PasswordHasher y CatalogQueryPort).
 * La clase está etiquetada como 'e2e' y deshabilitada salvo que RUN_E2E=true.
 */
@Tag("e2e")
@EnabledIfEnvironmentVariable(named = "RUN_E2E", matches = "true")
@SpringBootTest(classes = UserApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Testcontainers
class ClientsCreateE2EPostgresIT {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    @MockitoBean private PasswordHasher passwordHasher;
    @MockitoBean private CatalogQueryPort catalogQuery;

    private static final String BASE = "/clientes";
    private static final AtomicLong SEQ = new AtomicLong(1_000_000_000L); // números de identificación únicos

    @BeforeEach
    void setup() {
        when(passwordHasher.hash(anyString())).thenAnswer(inv -> "hash(" + inv.getArgument(0) + ")");
        when(catalogQuery.genderExists(anyString())).thenReturn(true);
        when(catalogQuery.identificationTypeExists(anyString())).thenReturn(true);
    }

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

    @Test
    @DisplayName("POST /clientes → 201 Created con cuerpo válido")
    void create_201() throws Exception {
        var json = jsonCreate(null);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("Ana")))
                .andExpect(jsonPath("$.identificationNumber", not(emptyString())));
    }

    @Test
    @DisplayName("POST /clientes → 400 Bad Request por @Valid (campo obligatorio inválido)")
    void create_400_validation() throws Exception {
        var badJson = jsonCreate(Map.of("address", "")); // dirección vacía

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.violations", not(empty())));
    }

    @Test
    @DisplayName("POST /clientes → 400 Bad Request por JSON malformado")
    void create_400_malformed_json() throws Exception {
        var malformed = "{\"firstName\":\"Ana\""; // sin cerrar

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Malformed JSON request")));
    }

    @Test
    @DisplayName("POST /clientes → 404 Not Found por catálogo inválido (gender)")
    void create_404_invalid_catalog_gender() throws Exception {
        when(catalogQuery.genderExists("BAD")).thenReturn(false);

        var json = jsonCreate(Map.of("gender", "BAD"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /clientes → 404 Not Found por catálogo inválido (identificationType)")
    void create_404_invalid_catalog_ident_type() throws Exception {
        when(catalogQuery.identificationTypeExists("XX")).thenReturn(false);

        var json = jsonCreate(Map.of("identificationType", "XX"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /clientes → 409 Conflict por identificación duplicada")
    void create_409_duplicate_identification() throws Exception {
        var dup = "1234567890";
        var first = jsonCreate(Map.of("identificationNumber", dup));
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isCreated());

        var second = jsonCreate(Map.of("identificationNumber", dup, "firstName", "Otra"));
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }

    @Test
    @DisplayName("POST /clientes → 500 Internal Server Error por excepción inesperada (hasher)")
    void create_500_unexpected() throws Exception {
        when(passwordHasher.hash(anyString())).thenThrow(new RuntimeException("boom"));

        var json = jsonCreate(null);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("Unexpected server error")));
    }
}
