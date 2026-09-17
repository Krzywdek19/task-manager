package pl.exceptionhandled.taskmanager.mapper.impl;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.taskmanager.dto.UserResponse;
import pl.exceptionhandled.taskmanager.entity.User;
import pl.exceptionhandled.taskmanager.mapper.UserMapper;

import java.util.Set;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserResponse userToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                Set.copyOf(user.getRoles())
        );
    }
}
