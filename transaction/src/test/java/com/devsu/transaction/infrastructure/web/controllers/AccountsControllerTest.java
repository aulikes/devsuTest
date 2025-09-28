package com.devsu.transaction.infrastructure.web.controllers;

import com.devsu.transaction.application.exception.AccountNotFoundException;
import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.application.service.ChangeAccountStatusService;
import com.devsu.transaction.application.service.CreateAccountService;
import com.devsu.transaction.application.service.GetAccountByNumberService;
import com.devsu.transaction.infrastructure.web.exception.GlobalExceptionHandler;
import com.devsu.transaction.infrastructure.web.mappers.AccountWebMapper;
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

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateAccountService createAccountService;

    @MockitoBean
    private ChangeAccountStatusService changeAccountStatusService;

    @MockitoBean
    private GetAccountByNumberService getAccountByNumberService;

    @MockitoBean
    private AccountWebMapper webMapper;

    @Test
    @DisplayName("POST /cuentas -> 201 y Location con el número de cuenta")
    void createAccountShouldReturn201AndLocation() throws Exception {
        AccountResult result = Mockito.mock(AccountResult.class, Mockito.RETURNS_DEEP_STUBS);
        when(result.accountNumber()).thenReturn("ACC-001");
        when(createAccountService.execute(any())).thenReturn(result);
        when(webMapper.toResponse(any())).thenReturn(null); // body no relevante

        String validPayload = """
            {
              "clientId": "123",
              "accountType": "AHORROS",
              "initialBalance": 1000.00
            }
            """;

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/cuentas/ACC-001")));
    }

    @Test
    @DisplayName("GET /cuentas/{accountNumber} -> 200 cuando existe")
    void getShouldReturn200WhenExists() throws Exception {
        AccountResult result = Mockito.mock(AccountResult.class);
        when(getAccountByNumberService.execute(eq("ACC-001"))).thenReturn(result);
        when(webMapper.toResponse(any())).thenReturn(null);

        mockMvc.perform(get("/cuentas/{accountNumber}", "ACC-001"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /cuentas/{accountNumber} -> 404 cuando no existe (mapeado por el advice)")
    void getShouldReturn404WhenNotFound() throws Exception {
        when(getAccountByNumberService.execute(eq("NOPE")))
                .thenThrow(new AccountNotFoundException("Account not found: NOPE"));
        when(webMapper.toResponse(any())).thenReturn(null);

        mockMvc.perform(get("/cuentas/{accountNumber}", "NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("PATCH /cuentas/{accountNumber}/estado -> 200 al cambiar estado")
    void changeStatusShouldReturn200() throws Exception {
        AccountResult result = Mockito.mock(AccountResult.class);
        when(changeAccountStatusService.execute(eq("ACC-001"), eq(true))).thenReturn(result);
        when(webMapper.toResponse(any())).thenReturn(null);

        String payload = """
            { "active": true }
            """;

        mockMvc.perform(patch("/cuentas/{accountNumber}/estado", "ACC-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /cuentas -> 400 cuando el payload es inválido")
    void createAccountShouldReturn400WhenInvalidPayload() throws Exception {
        String invalidPayload = "{}";

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
}
