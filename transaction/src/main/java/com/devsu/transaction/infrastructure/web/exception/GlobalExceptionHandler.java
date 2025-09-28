package com.devsu.transaction.infrastructure.web.exception;

import com.devsu.transaction.application.exception.AccountNotFoundException;
import com.devsu.transaction.application.exception.ClientNotFoundException;
import com.devsu.transaction.application.exception.DateReportException;
import com.devsu.transaction.application.exception.MovementNotFoundException;
import com.devsu.transaction.domain.exception.AccountNotPersistedException;
import com.devsu.transaction.domain.exception.InactiveAccountException;
import com.devsu.transaction.domain.exception.InsufficientFundsException;
import com.devsu.transaction.domain.exception.InvalidAmountException;
import com.devsu.transaction.infrastructure.web.dto.ErrorResponse;
import com.devsu.transaction.infrastructure.web.dto.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid en body (DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<FieldViolation> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                "Validation failed",
                request.getRequestURI(),
                violations
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // JSON mal formado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return badRequest("Malformed JSON request", request);
    }

    // Violaciones de integridad (únicos, FKs, etc.) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflict with database constraints", request);
    }

    // Tipos inválidos en path/query
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        return badRequest(message, request);
    }

    // @Validated en parámetros (fuera del body)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex,
                                                          HttpServletRequest request) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(cv -> new FieldViolation(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                "Constraint violation",
                request.getRequestURI(),
                violations
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest request
    ) {
        return badRequest(ex.getMessage(), request);
    }

    // 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex,
                                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Not Found", request);
    }

    // Método no soportado -> 405 (opcional pero útil)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", request);
    }

    // Argumentos ilegales desde aplicación/infra (p.ej., tipo de cuenta inválido)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        return badRequest(ex.getMessage(), request);
    }

    // Estados ilegales (p.ej., catálogo no seedado) -> 400 para no devolver 500
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
                                                            HttpServletRequest request) {
        return badRequest(ex.getMessage(), request);
    }

    // Fallback 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    // Cliente no encontrado (cuando consultamos al MS de clientes) -> 404
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFound(ClientNotFoundException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Cuenta no encontrada
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex,
                                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Movimiento no encontrado
    @ExceptionHandler(MovementNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMovementNotFoundException(MovementNotFoundException ex,
                                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Monto inválido (<= 0, cero, formato de negocio)
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException ex,
                                                             HttpServletRequest request) {
        return badRequest(ex.getMessage(), request);
    }

    // Fechas del reporte
    @ExceptionHandler(DateReportException.class)
    public ResponseEntity<ErrorResponse> handleIDateReport(DateReportException ex,
                                                             HttpServletRequest request) {
        return badRequest(ex.getMessage(), request);
    }

    // Fondos insuficientes -> 422
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    // Cuenta inactiva -> 409
    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponse> handleInactiveAccount(InactiveAccountException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Cuenta no persistida (sin id) -> 409
    @ExceptionHandler(AccountNotPersistedException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotPersisted(AccountNotPersistedException ex,
                                                                   HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Helpers
    private ResponseEntity<ErrorResponse> badRequest(String msg, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String msg, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.name(),
                msg,
                req.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(status).body(body);
    }
}
