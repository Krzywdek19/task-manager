package pl.exceptionhandled.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.exceptionhandled.taskmanager.entity.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByOwnerEmail(String email);

    Optional<Project> findByIdAndOwnerEmail(UUID id, String email);
}