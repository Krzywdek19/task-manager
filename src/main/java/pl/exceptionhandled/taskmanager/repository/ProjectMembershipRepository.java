package pl.exceptionhandled.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.exceptionhandled.taskmanager.entity.ProjectMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMembershipRepository
        extends JpaRepository<ProjectMembership, UUID> {

    boolean existsByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );

    Optional<ProjectMembership> findByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );

    List<ProjectMembership> findAllByProjectId(
            UUID projectId
    );
}