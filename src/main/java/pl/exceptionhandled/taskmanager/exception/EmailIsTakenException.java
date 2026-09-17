package pl.exceptionhandled.taskmanager.exception;

public class EmailIsTakenException extends RuntimeException {
    public EmailIsTakenException(String email) {
        super(String.format("Email: %s is already taken", email));
    }
}
