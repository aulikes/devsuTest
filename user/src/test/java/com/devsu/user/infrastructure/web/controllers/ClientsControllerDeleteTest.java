package com.devsu.user.infrastructure.web.controllers;

import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.service.CreateClientService;
import com.devsu.user.application.service.DeleteClientService;
import com.devsu.user.application.service.GetClientByClientIdService;
import com.devsu.user.application.service.GetClientByIdService;
import com.devsu.user.application.service.UpdateClientService;
import com.devsu.user.infrastructure.web.exception.GlobalExceptionHandler;
import com.devsu.user.infrastructure.web.mappers.ClientWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Este test cubre el endpoint DELETE /clientes/{id} verificando:
 * - 204 No Content en eliminación correcta.
 * - 404 Not Found cuando el cliente no existe.
 * - 400 Bad Request cuando el id no es numérico (type mismatch).
 */
@WebMvcTest(controllers = ClientsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ClientsControllerDeleteTest {

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
    @DisplayName("DELETE /clientes/{id} -> 204 No Content")
    void should_delete_and_return_204() throws Exception {
        Mockito.doNothing().when(deleteClientService).execute(10L);

        mockMvc.perform(delete(BASE + "/{id}", 10L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /clientes/{id} -> 404 Not Found cuando no existe")
    void should_return_404_when_client_not_found() throws Exception {
        Mockito.doThrow(new ClientNotFoundException("Client not found"))
                .when(deleteClientService).execute(eq(77L));

        mockMvc.perform(delete(BASE + "/{id}", 77L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.path", is(BASE + "/77")));
    }

    @Test
    @DisplayName("DELETE /clientes/{id} -> 400 Bad Request cuando id no es numérico")
    void should_return_400_on_type_mismatch() throws Exception {
        mockMvc.perform(delete(BASE + "/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.path", is(BASE + "/abc")));
    }
}
