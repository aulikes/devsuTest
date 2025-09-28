package com.devsu.user.infrastructure.web.controllers;

import com.devsu.user.application.command.CreateClientCommand;
import com.devsu.user.application.exception.InvalidCatalogCodeException;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.application.service.CreateClientService;
import com.devsu.user.application.service.DeleteClientService;
import com.devsu.user.application.service.GetClientByClientIdService;
import com.devsu.user.application.service.GetClientByIdService;
import com.devsu.user.application.service.UpdateClientService;
import com.devsu.user.infrastructure.web.dto.ClientResponse;
import com.devsu.user.infrastructure.web.dto.CreateClientRequest;
import com.devsu.user.infrastructure.web.exception.GlobalExceptionHandler;
import com.devsu.user.infrastructure.web.mappers.ClientWebMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Este test cubre el endpoint POST /clientes del ClientsController,
 * verificando estados 201, 400 (validación), 400 (JSON malformado),
 * 404 (catálogo inválido), 409 (violación de integridad) y 500 (genérico).
 */
@WebMvcTest(controllers = ClientsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClientsControllerCreateTest {

    private static final String BASE = "/clientes";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateClientService createClientService;
    @MockitoBean private UpdateClientService updateClientService;
    @MockitoBean private GetClientByIdService getClientByIdService;
    @MockitoBean private GetClientByClientIdService getClientByClientIdService;
    @MockitoBean private DeleteClientService deleteClientService;
    @MockitoBean private ClientWebMapper clientWebMapper;

    @Test
    @DisplayName("POST /clientes -> 201 Created con cuerpo de respuesta")
    void should_create_client_and_return_201() throws Exception {
        var req = new CreateClientRequest(
                "Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );

        var cmd = new CreateClientCommand(
                "Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );

        var result = new ClientResult(
                10L,"Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567",
                "ANA-001",true
        );

        var resp = new ClientResponse(
                10L,"Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),"CC","1234567890",
                "Calle 1 # 2-3","3001234567",
                "ANA-001",true
        );

        Mockito.when(clientWebMapper.toCommand(any(CreateClientRequest.class))).thenReturn(cmd);
        Mockito.when(createClientService.execute(eq(cmd))).thenReturn(result);
        Mockito.when(clientWebMapper.toResponse(eq(result))).thenReturn(resp);

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.clientId", is("ANA-001")))
                .andExpect(jsonPath("$.firstName", is("Ana")))
                .andExpect(jsonPath("$.status", is(true)));
    }

    @Test
    @DisplayName("POST /clientes -> 400 Bad Request por @Valid (body inválido)")
    void should_return_400_on_body_validation_error() throws Exception {
        var invalidReq = new CreateClientRequest(
                "Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "", "3001234567","pass123"
        );

        var json = objectMapper.writeValueAsString(invalidReq);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.path", is(BASE)))
                .andExpect(jsonPath("$.violations[0].field", not(emptyOrNullString())))
                .andExpect(jsonPath("$.violations[0].message", not(emptyOrNullString())));

        Mockito.verifyNoInteractions(clientWebMapper, createClientService);
    }

    @Test
    @DisplayName("POST /clientes -> 400 Bad Request por JSON malformado")
    void should_return_400_on_malformed_json() throws Exception {
        var malformed = "{\"firstName\":\"Ana\""; // falta cierre

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Malformed JSON request")))
                .andExpect(jsonPath("$.path", is(BASE)))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    @DisplayName("POST /clientes -> 404 Not Found por InvalidCatalogCodeException")
    void should_return_404_on_invalid_catalog() throws Exception {
        var req = new CreateClientRequest(
                "Ana","Gómez","BAD",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );
        var cmd = new CreateClientCommand(
                "Ana","Gómez","BAD",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );

        Mockito.when(clientWebMapper.toCommand(any(CreateClientRequest.class))).thenReturn(cmd);
        Mockito.when(createClientService.execute(eq(cmd)))
                .thenThrow(new InvalidCatalogCodeException("IdentificationType code not found: "));

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path", is(BASE)));
    }

    @Test
    @DisplayName("POST /clientes -> 409 Conflict por DataIntegrityViolationException")
    void should_return_409_on_integrity_violation() throws Exception {
        var req = new CreateClientRequest(
                "Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );
        var cmd = new CreateClientCommand(
                "Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );

        Mockito.when(clientWebMapper.toCommand(any(CreateClientRequest.class))).thenReturn(cmd);
        Mockito.when(createClientService.execute(eq(cmd)))
                .thenThrow(new DataIntegrityViolationException("unique_violation"));

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("Conflict with database constraints")))
                .andExpect(jsonPath("$.path", is(BASE)));
    }

    @Test
    @DisplayName("POST /clientes -> 500 Internal Server Error por excepción no controlada")
    void should_return_500_on_unexpected_exception() throws Exception {
        var req = new CreateClientRequest(
                "Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );
        var cmd = new CreateClientCommand(
                "Ana","Gómez","FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC","1234567890",
                "Calle 1 # 2-3","3001234567","pass123"
        );

        Mockito.when(clientWebMapper.toCommand(any(CreateClientRequest.class))).thenReturn(cmd);
        Mockito.when(createClientService.execute(eq(cmd)))
                .thenThrow(new RuntimeException("boom"));

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("Unexpected server error")))
                .andExpect(jsonPath("$.path", is(BASE)));
    }
}
