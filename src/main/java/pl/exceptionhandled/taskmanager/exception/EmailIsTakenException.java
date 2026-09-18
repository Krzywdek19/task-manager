package pl.exceptionhandled.taskmanager.exception;

import lombok.Getter;

@Getter
public class EmailIsTakenException extends ApiException {

    public EmailIsTakenException(String email) {
        super("Email: %s is already taken".formatted(email), ErrorCode.EMAIL_ALREADY_EXISTS);
    }

}
