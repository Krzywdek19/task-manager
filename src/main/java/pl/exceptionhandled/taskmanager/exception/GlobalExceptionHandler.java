package pl.exceptionhandled.taskmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

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

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProjectNotFound(
            ProjectNotFoundException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

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

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskNotFound(
            TaskNotFoundException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

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

    @ExceptionHandler(UserIsNotAssignedException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotAssigned(
            UserIsNotAssignedException ex,
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity
                .status(status)
                .body(buildResponse(
                        ErrorCode.INVALID_REQUEST,
                        "Malformed or invalid request body",
                        request,
                        status,
                        Map.of()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = Map.of(
                ex.getName(),
                "Invalid value: " + ex.getValue()
        );

        return ResponseEntity
                .status(status)
                .body(buildResponse(
                        ErrorCode.INVALID_REQUEST,
                        "Invalid request parameter",
                        request,
                        status,
                        fieldErrors
                ));
    }

    @ExceptionHandler(MembershipAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleMembershipAlreadyExists(
            MembershipAlreadyExistsException ex,
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

    @ExceptionHandler(ProjectOwnerCannotBeMemberException.class)
    public ResponseEntity<ApiErrorResponse> handleProjectOwnerCannotBeMember(
            ProjectOwnerCannotBeMemberException ex,
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

    @ExceptionHandler(MembershipNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMembershipNotFound(
            MembershipNotFoundException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

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

    @ExceptionHandler(UserIsNotProjectMemberException.class)
    public ResponseEntity<ApiErrorResponse> handleUserIsNotProjectMember(
            UserIsNotProjectMemberException ex,
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
