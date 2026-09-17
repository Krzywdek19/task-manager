package pl.exceptionhandled.taskmanager.exception;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(
                "Invalid email or password",
                ErrorCode.INVALID_CREDENTIALS
        );
    }
}