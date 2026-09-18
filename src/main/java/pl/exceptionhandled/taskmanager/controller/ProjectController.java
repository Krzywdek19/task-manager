package pl.exceptionhandled.taskmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.taskmanager.dto.CreateProjectRequest;
import pl.exceptionhandled.taskmanager.dto.ProjectResponse;
import pl.exceptionhandled.taskmanager.dto.UpdateProjectRequest;
import pl.exceptionhandled.taskmanager.service.ProjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @RequestBody @Valid CreateProjectRequest request,
            Authentication authentication
    ) {
        var project = projectService.create(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> findAll(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                projectService.findAllForUser(authentication.getName())
        );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> findById(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                projectService.findById(
                        projectId,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable UUID projectId,
            @RequestBody @Valid UpdateProjectRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                projectService.update(
                        projectId,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        projectService.delete(
                projectId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}