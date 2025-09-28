package com.devsu.user.infrastructure.web.controllers;

import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.application.service.CreateClientService;
import com.devsu.user.application.service.DeleteClientService;
import com.devsu.user.application.service.GetClientByClientIdService;
import com.devsu.user.application.service.GetClientByIdService;
import com.devsu.user.application.service.UpdateClientService;
import com.devsu.user.infrastructure.web.dto.ClientResponse;
import com.devsu.user.infrastructure.web.exception.GlobalExceptionHandler;
import com.devsu.user.infrastructure.web.mappers.ClientWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Esta clase valida los dos endpoints GET del ClientsController:
 * - GET /clientes/{id}
 * - GET /clientes/clientId/{clientId}
 */
@WebMvcTest(controllers = ClientsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClientsControllerGetTest {

    private static final String BASE = "/clientes";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CreateClientService createClientService;
    @MockitoBean private UpdateClientService updateClientService;
    @MockitoBean private GetClientByIdService getClientByIdService;
    @MockitoBean private GetClientByClientIdService getClientByClientIdService;
    @MockitoBean private DeleteClientService deleteClientService;
    @MockitoBean private ClientWebMapper clientWebMapper;

    @Test
    @DisplayName("GET /clientes/{id} -> 200 OK con el cuerpo esperado")
    void getById_shouldReturn200() throws Exception {
        // Se prepara el resultado de aplicación y el DTO de salida.
        var result = new ClientResult(
                10L, "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"),
                "CC", "1234567890",
                "Calle 1 # 2-3", "3001234567",
                "ANA-001", true
        );
        var resp = new ClientResponse(
                10L, "Ana", "Gómez", "FEMALE",
                LocalDate.parse("2000-01-01"), "CC", "1234567890",
                "Calle 1 # 2-3", "3001234567",
                "ANA-001", true
        );

        Mockito.when(getClientByIdService.execute(10L)).thenReturn(result);
        Mockito.when(clientWebMapper.toResponse(result)).thenReturn(resp);

        mockMvc.perform(get(BASE + "/{id}", 10L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.clientId", is("ANA-001")))
                .andExpect(jsonPath("$.firstName", is("Ana")))
                .andExpect(jsonPath("$.status", is(true)));
    }

    @Test
    @DisplayName("GET /clientes/{id} -> 404 Not Found cuando el cliente no existe")
    void getById_shouldReturn404_whenNotFound() throws Exception {
        // Se simula ausencia del recurso en el caso de uso.
        Mockito.when(getClientByIdService.execute(99L)).thenThrow(new ClientNotFoundException("Client not found"));

        mockMvc.perform(get(BASE + "/{id}", 99L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.path", is(BASE + "/99")));

        // Se verifica que el mapper no se invoque cuando hay excepción.
        verifyNoInteractions(clientWebMapper);
    }

    @Test
    @DisplayName("GET /clientes/{id} -> 400 Bad Request cuando el id no es numérico")
    void getById_shouldReturn400_onTypeMismatch() throws Exception {
        // Se envía un valor no numérico; el handler debe devolver 400.
        mockMvc.perform(get(BASE + "/{id}", "abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'id'")))
                .andExpect(jsonPath("$.path", is(BASE + "/abc")));

        verifyNoInteractions(getClientByIdService, clientWebMapper);
    }

    @Test
    @DisplayName("GET /clientes/clientId/{clientId} -> 200 OK con el cuerpo esperado")
    void getByClientId_shouldReturn200() throws Exception {
        var result = new ClientResult(
                22L, "Carlos", "López", "MALE",
                LocalDate.parse("1995-05-05"),
                "CE", "88888888",
                "Av. 123", "3021112233",
                "CAR-123", true
        );
        var resp = new ClientResponse(
                22L, "Carlos", "López", "MALE",
                LocalDate.parse("1995-05-05"), "CE", "88888888",
                "Av. 123", "3021112233",
                "CAR-123", true
        );

        Mockito.when(getClientByClientIdService.execute("CAR-123")).thenReturn(result);
        Mockito.when(clientWebMapper.toResponse(eq(result))).thenReturn(resp);

        mockMvc.perform(get(BASE + "/clientId/{clientId}", "CAR-123").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(22)))
                .andExpect(jsonPath("$.clientId", is("CAR-123")))
                .andExpect(jsonPath("$.identificationType", is("CE")));
    }

    @Test
    @DisplayName("GET /clientes/clientId/{clientId} -> 404 Not Found cuando el cliente no existe")
    void getByClientId_shouldReturn404_whenNotFound() throws Exception {
        Mockito.when(getClientByClientIdService.execute("NOPE")).thenThrow(new ClientNotFoundException("NOPE"));

        mockMvc.perform(get(BASE + "/clientId/{clientId}", "NOPE").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.path", is(BASE + "/clientId/NOPE")));

        verifyNoInteractions(clientWebMapper);
    }
}
