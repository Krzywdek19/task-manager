package pl.exceptionhandled.taskmanager.mapper;

import pl.exceptionhandled.taskmanager.dto.TaskResponse;
import pl.exceptionhandled.taskmanager.entity.Task;

public interface TaskMapper {
    TaskResponse taskToResponse(Task task);
}
