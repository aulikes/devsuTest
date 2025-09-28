package com.devsu.transaction.infrastructure.web.controllers;

import com.devsu.transaction.application.result.AccountStatementReport;
import com.devsu.transaction.application.service.AccountStatementReportService;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
public class ReportsController {

    private final AccountStatementReportService reportService;

    public ReportsController(AccountStatementReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public AccountStatementReport getAccountStatement(
            @RequestParam("clientId") @NotNull String clientId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.execute(clientId, from, to);
    }
}
