package pl.exceptionhandled.taskmanager.dto;

import pl.exceptionhandled.taskmanager.entity.Role;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Set<Role> roles
) {
}
