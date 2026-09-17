package pl.exceptionhandled.taskmanager.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pl.exceptionhandled.taskmanager.entity.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRejectDuplicateEmail() {
        User firstUser = new User();
        firstUser.setEmail("test@example.com");
        firstUser.setPasswordHash("hash");

        User secondUser = new User();
        secondUser.setEmail("test@example.com");
        secondUser.setPasswordHash("hash");

        userRepository.saveAndFlush(firstUser);

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
