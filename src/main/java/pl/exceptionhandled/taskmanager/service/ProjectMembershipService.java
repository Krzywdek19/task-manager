package pl.exceptionhandled.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.taskmanager.dto.ProjectMemberResponse;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.entity.ProjectMembership;
import pl.exceptionhandled.taskmanager.entity.ProjectRole;
import pl.exceptionhandled.taskmanager.exception.MembershipAlreadyExistsException;
import pl.exceptionhandled.taskmanager.exception.MembershipNotFoundException;
import pl.exceptionhandled.taskmanager.exception.ProjectNotFoundException;
import pl.exceptionhandled.taskmanager.exception.ProjectOwnerCannotBeMemberException;
import pl.exceptionhandled.taskmanager.exception.UserNotFoundException;
import pl.exceptionhandled.taskmanager.mapper.ProjectMembershipMapper;
import pl.exceptionhandled.taskmanager.repository.ProjectMembershipRepository;
import pl.exceptionhandled.taskmanager.repository.ProjectRepository;
import pl.exceptionhandled.taskmanager.repository.TaskRepository;
import pl.exceptionhandled.taskmanager.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMembershipService {

    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectMembershipMapper projectMembershipMapper;

    @Transactional
    public ProjectMemberResponse addMember(
            String ownerEmail,
            String memberEmail,
            UUID projectId
    ) {
        var project = getProjectForOwner(projectId, ownerEmail);

        var normalizedMemberEmail = memberEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        var user = userRepository
                .findByEmail(normalizedMemberEmail)
                .orElseThrow(() ->
                        new UserNotFoundException(normalizedMemberEmail)
                );

        if (project.getOwner().getId().equals(user.getId())) {
            throw new ProjectOwnerCannotBeMemberException();
        }

        if (projectMembershipRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        user.getId()
                )) {

            throw new MembershipAlreadyExistsException(
                    user.getEmail(),
                    projectId
            );
        }

        var membership = ProjectMembership.builder()
                .role(ProjectRole.MEMBER)
                .project(project)
                .user(user)
                .build();

        try {
            var savedMembership =
                    projectMembershipRepository.saveAndFlush(membership);

            return projectMembershipMapper.toResponse(savedMembership);

        } catch (DataIntegrityViolationException ex) {

            if (isMembershipUniqueConstraintViolation(ex)) {
                throw new MembershipAlreadyExistsException(
                        user.getEmail(),
                        projectId
                );
            }

            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> findAllMembers(
            String ownerEmail,
            UUID projectId
    ) {
        getProjectForOwner(projectId, ownerEmail);

        return projectMembershipRepository
                .findAllByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .map(projectMembershipMapper::toResponse)
                .toList();
    }

    @Transactional
    public void removeMember(
            String ownerEmail,
            UUID projectId,
            UUID userId
    ) {
        getProjectForOwner(projectId, ownerEmail);

        var membership = projectMembershipRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                )
                .orElseThrow(() ->
                        new MembershipNotFoundException(
                                userId,
                                projectId
                        )
                );

        taskRepository.deleteAssignmentsForUserInProject(
                projectId,
                userId
        );

        projectMembershipRepository.delete(membership);
    }

    private boolean isMembershipUniqueConstraintViolation(
            DataIntegrityViolationException ex
    ) {
        Throwable cause = ex;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation) {
                return "uq_project_memberships_project_user"
                        .equals(constraintViolation.getConstraintName());
            }

            cause = cause.getCause();
        }

        return false;
    }

    private Project getProjectForOwner(
            UUID projectId,
            String ownerEmail
    ) {
        return projectRepository
                .findByIdAndOwnerEmail(
                        projectId,
                        ownerEmail
                )
                .orElseThrow(() ->
                        new ProjectNotFoundException(projectId)
                );
    }
}