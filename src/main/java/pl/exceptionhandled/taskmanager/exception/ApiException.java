package pl.exceptionhandled.taskmanager.exception;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {
    private final ErrorCode code;

    public ApiException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }
}
