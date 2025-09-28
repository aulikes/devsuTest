package com.devsu.user.infrastructure.web.exception;

import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.exception.DuplicateIdentificationException;
import com.devsu.user.application.exception.InvalidCatalogCodeException;
import com.devsu.user.infrastructure.web.dto.ErrorResponse;
import com.devsu.user.infrastructure.web.dto.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    // Se maneja cualquier excepción no contemplada específicamente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFound(ClientNotFoundException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Se maneja el error de catálogo inválido definido en la capa application
    @ExceptionHandler(InvalidCatalogCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCatalog(InvalidCatalogCodeException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateIdentificationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateIdentificationException ex,
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

