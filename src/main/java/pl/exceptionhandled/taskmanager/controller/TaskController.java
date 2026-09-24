package pl.exceptionhandled.taskmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.taskmanager.dto.CreateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.TaskResponse;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskRequest;
import pl.exceptionhandled.taskmanager.dto.UpdateTaskStatusRequest;
import pl.exceptionhandled.taskmanager.entity.TaskPriority;
import pl.exceptionhandled.taskmanager.entity.TaskStatus;
import pl.exceptionhandled.taskmanager.service.TaskService;

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
    public ResponseEntity<Page<TaskResponse>> findAll(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.findAll(
                        projectId,
                        authentication.getName(),
                        status,
                        priority,
                        pageable
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

    @PutMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<TaskResponse> assignUser(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.assignUser(
                        projectId,
                        taskId,
                        userId,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody @Valid UpdateTaskStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.changeStatus(
                        projectId,
                        taskId,
                        request,
                        authentication.getName()
                )
        );
    }


    @DeleteMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<TaskResponse> unassignUser(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.unassignUser(
                        projectId,
                        taskId,
                        userId,
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