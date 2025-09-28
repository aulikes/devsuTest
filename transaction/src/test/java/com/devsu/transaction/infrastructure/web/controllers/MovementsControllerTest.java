package com.devsu.transaction.infrastructure.web.controllers;

import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.application.service.CreateMovementService;
import com.devsu.transaction.application.service.ListMovementsByDateService;
import com.devsu.transaction.domain.exception.InsufficientFundsException;
import com.devsu.transaction.infrastructure.web.exception.GlobalExceptionHandler;
import com.devsu.transaction.infrastructure.web.mappers.MovementReadAssembler;
import com.devsu.transaction.infrastructure.web.mappers.MovementWebMapper;
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
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MovementsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        MovementReadAssembler.class,
        GlobalExceptionHandler.class
})
class MovementsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateMovementService createMovementService;

    @MockitoBean
    private ListMovementsByDateService listMovementsByDateService;

    @MockitoBean
    private MovementWebMapper mapper;

    @Test
    @DisplayName("POST /movimientos debe responder 201 y Location al crear un movimiento")
    void createShouldReturn201AndLocation() throws Exception {
        MovementResult result = Mockito.mock(MovementResult.class, Mockito.RETURNS_DEEP_STUBS);
        when(result.id()).thenReturn(1L);
        when(createMovementService.execute(any())).thenReturn(result);
        when(mapper.toResponse(any())).thenReturn(null);

        String payload = """
            {
              "accountNumber": "1234567890",
              "amount": 100.50,
              "description": "Depósito"
            }
            """;

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/movimientos/1")));
    }

    @Test
    @DisplayName("POST /movimientos debe responder 422 cuando no hay fondos suficientes")
    void createShouldReturn422InsufficientFundsHandledByAdvice() throws Exception {
        when(createMovementService.execute(any()))
                .thenThrow(new InsufficientFundsException("Saldo insuficiente"));
        when(mapper.toResponse(any())).thenReturn(null);

        String payload = """
            {
              "accountNumber": "1234567890",
              "amount": 999999.99,
              "description": "Retiro"
            }
            """;

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /movimientos debe responder 400 cuando el payload es inválido")
    void createShouldReturn400WhenPayloadInvalid() throws Exception {
        String invalidPayload = "{}";

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    // -------- GET /movimientos --------

    @Test
    @DisplayName("GET /movimientos debe responder 200 y lista vacía cuando no hay movimientos")
    void listShouldReturn200Empty() throws Exception {
        String accountNumber = "ACC-001";
        LocalDate from = LocalDate.parse("2025-01-01");
        LocalDate to = LocalDate.parse("2025-01-31");
        when(listMovementsByDateService.execute(accountNumber, from, to)).thenReturn(List.of());

        mockMvc.perform(get("/movimientos")
                        .param("accountNumber", accountNumber)
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(listMovementsByDateService).execute(accountNumber, from, to);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("GET /movimientos debe responder 200 y un arreglo con N elementos")
    void listShouldReturn200WithElements() throws Exception {
        String accountNumber = "ACC-002";
        LocalDate from = LocalDate.parse("2025-02-01");
        LocalDate to = LocalDate.parse("2025-02-28");

        MovementResult r1 = Mockito.mock(MovementResult.class);
        MovementResult r2 = Mockito.mock(MovementResult.class);
        when(listMovementsByDateService.execute(accountNumber, from, to))
                .thenReturn(List.of(r1, r2));
        when(mapper.toResponse(any())).thenReturn(null);

        mockMvc.perform(get("/movimientos")
                        .param("accountNumber", accountNumber)
                        .param("from", "2025-02-01")
                        .param("to", "2025-02-28"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(listMovementsByDateService).execute(accountNumber, from, to);
        verify(mapper, times(2)).toResponse(any());
    }
}
