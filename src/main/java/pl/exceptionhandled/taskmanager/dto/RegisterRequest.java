package pl.exceptionhandled.taskmanager.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @NotBlank
        @Size(min = 8, max = 64)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
                message = "Password must contain at least one lowercase letter, one uppercase letter, one digit and one special character"
        )
        String password
) {
}
