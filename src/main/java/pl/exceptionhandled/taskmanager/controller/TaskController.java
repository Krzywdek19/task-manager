package pl.exceptionhandled.taskmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.taskmanager.dto.CreateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.TaskResponse;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskRequest;
import pl.exceptionhandled.taskmanager.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @PathVariable UUID projectId,
            @RequestBody @Valid CreateTaskRequest request,
            Authentication authentication
    ) {
        var task = taskService.create(
                projectId,
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(task);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> findAll(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.findAll(
                        projectId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> findById(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.findById(
                        projectId,
                        taskId,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody @Valid UpdateTaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.update(
                        projectId,
                        taskId,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            Authentication authentication
    ) {
        taskService.delete(
                projectId,
                taskId,
                authentication.getName()
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}