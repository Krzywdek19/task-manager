package pl.exceptionhandled.taskmanager.mapper;

import pl.exceptionhandled.taskmanager.dto.UserResponse;
import pl.exceptionhandled.taskmanager.entity.User;

public interface UserMapper {
    UserResponse userToResponse(User user);
}
