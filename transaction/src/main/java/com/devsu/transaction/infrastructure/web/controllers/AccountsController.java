package com.devsu.transaction.infrastructure.web.controllers;

import com.devsu.transaction.application.command.CreateAccountCommand;
import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.application.service.ChangeAccountStatusService;
import com.devsu.transaction.application.service.CreateAccountService;
import com.devsu.transaction.application.service.GetAccountByNumberService;
import com.devsu.transaction.infrastructure.web.dto.AccountResponse;
import com.devsu.transaction.infrastructure.web.dto.ChangeAccountStatusRequest;
import com.devsu.transaction.infrastructure.web.dto.CreateAccountRequest;
import com.devsu.transaction.infrastructure.web.mappers.AccountWebMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class AccountsController {

    private final CreateAccountService createAccountService;
    private final ChangeAccountStatusService changeAccountStatusService;
    private final GetAccountByNumberService getAccountService;
    private final AccountWebMapper webMapper;

    /** Crea una cuenta (número generado en servidor, estado siempre activo, saldo inicial ≥ 0). */
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        CreateAccountCommand cmd = webMapper.toCommand(request);
        AccountResult result = createAccountService.execute(cmd);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{accountNumber}")
                .buildAndExpand(result.accountNumber())
                .toUri();

        return ResponseEntity.created(location).body(webMapper.toResponse(result));
    }

    /** Modifica el estado de la cuenta (activar/desactivar). */
    @PatchMapping("/{accountNumber}/estado")
    public ResponseEntity<AccountResponse> changeStatus(@PathVariable String accountNumber,
                                                        @Valid @RequestBody ChangeAccountStatusRequest req) {
        AccountResult result = changeAccountStatusService.execute(accountNumber, req.active());
        return ResponseEntity.ok(webMapper.toResponse(result));
    }

    /** GET /cuentas/{accountNumber} -> detalle de la cuenta. */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getByAccountNumber(
            @PathVariable
            @NotBlank @Size(max = 32)
            String accountNumber
    ) {
        var result = getAccountService.execute(accountNumber);
        return ResponseEntity.ok(webMapper.toResponse(result));
    }
}
