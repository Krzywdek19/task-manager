package pl.exceptionhandled.taskmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.exceptionhandled.taskmanager.dto.ApiErrorResponse;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailIsTakenException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailIsTaken(
            EmailIsTakenException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        return ResponseEntity
                .status(status)
                .body(buildResponse(
                        ex.getCode(),
                        ex.getMessage(),
                        request,
                        status,
                        Map.of()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Invalid value",
                        (first, second) -> first
                ));

        return ResponseEntity
                .status(status)
                .body(buildResponse(
                        ErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        request,
                        status,
                        fieldErrors
                ));
    }

    private ApiErrorResponse buildResponse(
            ErrorCode code,
            String message,
            HttpServletRequest request,
            HttpStatus status,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code.name(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
    }
}
