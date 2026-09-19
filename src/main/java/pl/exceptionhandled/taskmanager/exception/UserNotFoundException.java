package pl.exceptionhandled.taskmanager.exception;

import java.util.UUID;

public class UserNotFoundException extends ApiException{
    public UserNotFoundException(String email) {
        super("User with email: %s not found".formatted(email), ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(UUID id) {
        super("User with id: %s not found".formatted(id.toString()), ErrorCode.USER_NOT_FOUND);
    }
}
