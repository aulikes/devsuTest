package com.devsu.transaction.infrastructure.web.controllers;

import com.devsu.transaction.application.command.CreateMovementCommand;
import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.application.service.CreateMovementService;
import com.devsu.transaction.application.service.ListMovementsByDateService;
import com.devsu.transaction.infrastructure.web.dto.CreateMovementRequest;
import com.devsu.transaction.infrastructure.web.dto.MovementResponse;
import com.devsu.transaction.infrastructure.web.mappers.MovementReadAssembler;
import com.devsu.transaction.infrastructure.web.mappers.MovementWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Controller REST para registrar movimientos.
 * - POST /movimientos: registra un movimiento (positivo=depósito, negativo=retiro).
 * - La validación semántica definitiva vive en el dominio.
 */
@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovementsController {

    private final CreateMovementService createMovementService;
    private final ListMovementsByDateService listMovementsByDateService;
    private final MovementWebMapper mapper;
    private final MovementReadAssembler readAssembler;

    @PostMapping
    public ResponseEntity<MovementResponse> create(@Valid @RequestBody CreateMovementRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        CreateMovementCommand command = mapper.toCommand(request);
        MovementResult result = createMovementService.execute(command);

        var location = uriBuilder.path("/movimientos/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toResponse(result));
    }

    // GET: listar movimientos por cuenta y rango de fechas
    @GetMapping
    public ResponseEntity<List<MovementResponse>> listByAccountAndRange(
            @RequestParam String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate to) {

        var results = listMovementsByDateService.execute(accountNumber, from, to);

        var body = results.stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(body);
    }
}
