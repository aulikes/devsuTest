package com.devsu.transaction.infrastructure.web.controllers;

import com.devsu.transaction.application.exception.ClientNotFoundException;
import com.devsu.transaction.application.result.AccountStatementReport;
import com.devsu.transaction.application.service.AccountStatementReportService;
import com.devsu.transaction.infrastructure.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountStatementReportService reportService;

    @Test
    @DisplayName("GET /reportes -> 200 con parámetros válidos")
    void shouldReturn200WithValidParams() throws Exception {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to   = LocalDate.of(2025, 1, 31);

        // Devolvemos null para no acoplarnos al JSON del reporte (solo validamos el 200)
        when(reportService.execute(eq("CLI-1"), eq(from), eq(to))).thenReturn((AccountStatementReport) null);

        mockMvc.perform(get("/reportes")
                        .param("clientId", "CLI-1")
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31"))
                .andExpect(status().isOk());

        verify(reportService).execute("CLI-1", from, to);
    }

    @Test
    @DisplayName("GET /reportes -> 404 when clientId does not exist")
    void shouldReturn404WhenClientNotFound() throws Exception {
        when(reportService.execute(eq("CLI-404"), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new ClientNotFoundException("CLI-404"));

        mockMvc.perform(get("/reportes")
                        .param("clientId", "CLI-404")
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /reportes -> 400 si falta parámetro obligatorio")
    void shouldReturn400WhenParamMissing() throws Exception {
        // Falta clientId
        mockMvc.perform(get("/reportes")
                        .param("from", "2025-01-01")
                        .param("to", "2025-01-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /reportes -> 400 si el formato de fecha es inválido")
    void shouldReturn400WhenInvalidDate() throws Exception {
        mockMvc.perform(get("/reportes")
                        .param("clientId", "CLI-1")
                        .param("from", "2025-99-99") // fecha no valida
                        .param("to", "2025-01-31"))
                .andExpect(status().isBadRequest());
    }
}
