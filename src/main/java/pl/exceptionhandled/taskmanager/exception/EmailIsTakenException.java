package pl.exceptionhandled.taskmanager.exception;

import lombok.Getter;

@Getter
public class EmailIsTakenException extends ApiException {

    public EmailIsTakenException(String email) {
        super(String.format("Email: %s is already taken", email), ErrorCode.EMAIL_ALREADY_EXISTS);
    }

}
