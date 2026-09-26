package pl.exceptionhandled.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddProjectMemberRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email
) {
}