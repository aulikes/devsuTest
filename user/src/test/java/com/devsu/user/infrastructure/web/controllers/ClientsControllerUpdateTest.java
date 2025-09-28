// src/test/java/com/devsu/user/infrastructure/web/controllers/ClientsControllerUpdateTest.java
package com.devsu.user.infrastructure.web.controllers;

import com.devsu.user.application.command.UpdateClientCommand;
import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.exception.InvalidCatalogCodeException;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.application.service.CreateClientService;
import com.devsu.user.application.service.DeleteClientService;
import com.devsu.user.application.service.GetClientByClientIdService;
import com.devsu.user.application.service.GetClientByIdService;
import com.devsu.user.application.service.UpdateClientService;
import com.devsu.user.infrastructure.web.dto.ClientResponse;
import com.devsu.user.infrastructure.web.dto.UpdateClientRequest;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Esta clase valida el endpoint PUT /clientes/{id} del ClientsController.
 * Se mockean los casos de uso y el ClientWebMapper con @MockitoBean y se importa el GlobalExceptionHandler.
 * Se cubren estados: 200 OK, 400 Bad Request (por @Valid y por type mismatch), 404 Not Found,
 * 404 Not Found por InvalidCatalogCodeException, 409 Conflict y 500 Internal Server Error.
 */
@WebMvcTest(controllers = ClientsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClientsControllerUpdateTest {

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
    @DisplayName("PUT /clientes/{id} → 200 OK con el cuerpo actualizado")
    void should_update_and_return_200() throws Exception {
        var pathId = 10L;

        var req = new UpdateClientRequest(
                "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"),
                "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000", "newPass123"
        );

        var cmd = new UpdateClientCommand(
                pathId, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"),
                "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000", "newPass123"
        );

        var result = new ClientResult(
                pathId, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"),
                "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000",
                "ANA-001", true
        );

        var resp = new ClientResponse(
                pathId, "Ana María", "Gómez R.", "FEMALE",
                LocalDate.parse("2000-02-02"), "CC", "1234567890",
                "Calle 10 # 20-30", "3015550000",
                "ANA-001", true
        );

        Mockito.when(clientWebMapper.toCommand(eq(pathId), any(UpdateClientRequest.class))).thenReturn(cmd);
        Mockito.when(updateClientService.execute(eq(cmd))).thenReturn(result);
        Mockito.when(clientWebMapper.toResponse(eq(result))).thenReturn(resp);

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(BASE + "/{id}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.firstName", is("Ana María")))
                .andExpect(jsonPath("$.clientId", is("ANA-001")))
                .andExpect(jsonPath("$.status", is(true)));
    }

    @Test
    @DisplayName("PUT /clientes/{id} → 400 Bad Request por @Valid (body inválido)")
    void should_return_400_on_body_validation_error() throws Exception {
        var req = new UpdateClientRequest(
                "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-02-02"),
                "CC", "1234567890",
                "", "3015550000", null
        );

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(BASE + "/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.path", is(BASE + "/10")));

        Mockito.verifyNoInteractions(updateClientService, clientWebMapper);
    }

    @Test
    @DisplayName("PUT /clientes/{id} → 400 Bad Request cuando el id no es numérico")
    void should_return_400_on_type_mismatch() throws Exception {
        var req = new UpdateClientRequest(
                "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-02-02"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );
        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(BASE + "/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'id'")))
                .andExpect(jsonPath("$.path", is(BASE + "/abc")));

        Mockito.verifyNoInteractions(updateClientService, clientWebMapper);
    }

    @Test
    @DisplayName("PUT /clientes/{id} → 404 Not Found cuando el cliente no existe")
    void should_return_404_when_not_found() throws Exception {
        var pathId = 99L;

        var req = new UpdateClientRequest(
                "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );
        var cmd = new UpdateClientCommand(
                pathId, "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );

        Mockito.when(clientWebMapper.toCommand(eq(pathId), any(UpdateClientRequest.class))).thenReturn(cmd);
        Mockito.when(updateClientService.execute(eq(cmd))).thenThrow(new ClientNotFoundException("Client not found"));

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(BASE + "/{id}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.path", is(BASE + "/99")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} → 404 Not Found por InvalidCatalogCodeException")
    void should_return_404_on_invalid_catalog() throws Exception {
        var pathId = 10L;

        var req = new UpdateClientRequest(
                "Ana", "Gómez", "BAD",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );
        var cmd = new UpdateClientCommand(
                pathId, "Ana", "Gómez", "BAD",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );

        Mockito.when(clientWebMapper.toCommand(eq(pathId), any(UpdateClientRequest.class))).thenReturn(cmd);
        Mockito.when(updateClientService.execute(eq(cmd)))
                .thenThrow(new InvalidCatalogCodeException("Gender code not found"));

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(BASE + "/{id}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path", is(BASE + "/10")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} → 409 Conflict por violación de integridad")
    void should_return_409_on_integrity_violation() throws Exception {
        var pathId = 10L;

        var req = new UpdateClientRequest(
                "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );
        var cmd = new UpdateClientCommand(
                pathId, "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );

        Mockito.when(clientWebMapper.toCommand(eq(pathId), any(UpdateClientRequest.class))).thenReturn(cmd);
        Mockito.when(updateClientService.execute(eq(cmd)))
                .thenThrow(new DataIntegrityViolationException("unique_violation"));

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(BASE + "/{id}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("Conflict with database constraints")))
                .andExpect(jsonPath("$.path", is(BASE + "/10")));
    }

    @Test
    @DisplayName("PUT /clientes/{id} → 500 Internal Server Error por excepción no controlada")
    void should_return_500_on_unexpected_exception() throws Exception {
        var pathId = 10L;

        var req = new UpdateClientRequest(
                "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );
        var cmd = new UpdateClientCommand(
                pathId, "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle", "300", "newPass123"
        );

        Mockito.when(clientWebMapper.toCommand(eq(pathId), any(UpdateClientRequest.class))).thenReturn(cmd);
        Mockito.when(updateClientService.execute(eq(cmd))).thenThrow(new RuntimeException("boom"));

        var json = objectMapper.writeValueAsString(req);

        mockMvc.perform(put(BASE + "/{id}", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("Unexpected server error")))
                .andExpect(jsonPath("$.path", is(BASE + "/10")));
    }
}
