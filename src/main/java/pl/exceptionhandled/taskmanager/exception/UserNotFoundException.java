package pl.exceptionhandled.taskmanager.exception;

public class UserNotFoundException extends ApiException{
    public UserNotFoundException(String email) {
        super("User with email: %s not found".formatted(email), ErrorCode.USER_NOT_FOUND);
    }
}
