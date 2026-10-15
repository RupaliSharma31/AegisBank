package ledger_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import ledger_service.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LedgerException.class)
    public ResponseEntity<ErrorResponse> ledger(
            LedgerException e,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(e.getStatus())
                .body(
                        new ErrorResponse(
                                Instant.now(),
                                e.getStatus().value(),
                                e.getCode(),
                                e.getMessage(),
                                request.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorResponse(
                                Instant.now(),
                                400,
                                "VALIDATION_ERROR",
                                message,
                                request.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badInput(
            IllegalArgumentException e,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorResponse(
                                Instant.now(),
                                400,
                                "BAD_REQUEST",
                                e.getMessage(),
                                request.getRequestURI()
                        )
                );
    }
}
