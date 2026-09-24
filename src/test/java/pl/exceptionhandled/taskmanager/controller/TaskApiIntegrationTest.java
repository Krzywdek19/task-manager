package pl.exceptionhandled.taskmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.http.MediaType;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pl.exceptionhandled.taskmanager.entity.Project;
import pl.exceptionhandled.taskmanager.entity.Role;
import pl.exceptionhandled.taskmanager.entity.User;
import pl.exceptionhandled.taskmanager.repository.ProjectRepository;
import pl.exceptionhandled.taskmanager.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TaskApiIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;


    @Test
    void createTask_shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/projects/{projectId}/tasks", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Integration task",
                              "description": "Test",
                              "priority": "HIGH"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_shouldReturn201ForAuthenticatedProjectOwner() throws Exception {
        var owner = new User();
        owner.setEmail("owner@test.com");
        owner.setPasswordHash("test-hash");
        owner.setRoles(new HashSet<>(Set.of(Role.USER)));

        owner = userRepository.saveAndFlush(owner);

        var project = Project.builder()
                .name("Integration project")
                .owner(owner)
                .build();

        project = projectRepository.saveAndFlush(project);

        User finalOwner = owner;
        mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", project.getId())
                                .with(jwt().jwt(jwt -> jwt.subject(finalOwner.getEmail())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "title": "Integration task",
                          "description": "Test",
                          "priority": "HIGH"
                        }
                        """)
                )
                .andExpect(status().isCreated());
    }
}
