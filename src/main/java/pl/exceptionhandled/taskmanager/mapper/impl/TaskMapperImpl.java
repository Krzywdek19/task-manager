package pl.exceptionhandled.taskmanager.mapper.impl;

import org.springframework.stereotype.Component;
import pl.exceptionhandled.taskmanager.dto.TaskResponse;
import pl.exceptionhandled.taskmanager.entity.Task;
import pl.exceptionhandled.taskmanager.entity.User;
import pl.exceptionhandled.taskmanager.mapper.TaskMapper;

import java.util.stream.Collectors;

@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public TaskResponse taskToResponse(Task task) {
        var assigneeIds = task.getAssignees()
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getProject().getId(),
                assigneeIds,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}