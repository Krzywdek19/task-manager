package pl.exceptionhandled.taskmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.taskmanager.dto.AddProjectMemberRequest;
import pl.exceptionhandled.taskmanager.dto.ProjectMemberResponse;
import pl.exceptionhandled.taskmanager.service.ProjectMembershipService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMembershipController {

    private final ProjectMembershipService projectMembershipService;

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable UUID projectId,
            @RequestBody @Valid AddProjectMemberRequest request,
            Authentication authentication
    ) {
        var member = projectMembershipService.addMember(
                authentication.getName(),
                request.email(),
                projectId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(member);
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> findAllMembers(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                projectMembershipService.findAllMembers(
                        authentication.getName(),
                        projectId
                )
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        projectMembershipService.removeMember(
                authentication.getName(),
                projectId,
                userId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}