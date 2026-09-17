package pl.exceptionhandled.taskmanager.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.exceptionhandled.taskmanager.dto.RegisterRequest;
import pl.exceptionhandled.taskmanager.dto.UserResponse;
import pl.exceptionhandled.taskmanager.entity.Role;
import pl.exceptionhandled.taskmanager.entity.User;
import pl.exceptionhandled.taskmanager.exception.EmailIsTakenException;
import pl.exceptionhandled.taskmanager.mapper.UserMapper;
import pl.exceptionhandled.taskmanager.repository.UserRepository;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceConcurrencyTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldHandleRaceConditionWhenSameEmailIsRegisteredConcurrently()
            throws Exception {

        String email = "race@example.com";

        RegisterRequest request =
                new RegisterRequest(email, "Password123!");

        CyclicBarrier existsBarrier = new CyclicBarrier(2);
        AtomicInteger saveAttempts = new AtomicInteger();

        when(repository.existsByEmail(email))
                .thenAnswer(invocation -> {
                    existsBarrier.await();

                    return false;
                });

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashed-password");

        when(repository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    if (saveAttempts.incrementAndGet() == 1) {
                        return user;
                    }

                    SQLException sqlException =
                            new SQLException("duplicate key");

                    ConstraintViolationException constraintException =
                            new ConstraintViolationException(
                                    "duplicate email",
                                    sqlException,
                                    "uq_users_email"
                            );

                    throw new DataIntegrityViolationException(
                            "duplicate email",
                            constraintException
                    );
                });

        when(mapper.userToResponse(any(User.class)))
                .thenReturn(
                        new UserResponse(
                                UUID.randomUUID(),
                                email,
                                Set.of(Role.USER)
                        )
                );

        Callable<Object> registration = () -> {
            try {
                return authService.register(request);
            } catch (RuntimeException exception) {
                return exception;
            }
        };

        Object firstResult;
        Object secondResult;

        try (var executor =
                     Executors.newVirtualThreadPerTaskExecutor()) {

            Future<Object> first =
                    executor.submit(registration);

            Future<Object> second =
                    executor.submit(registration);

            firstResult = first.get();
            secondResult = second.get();
        }

        long successfulRegistrations =
                Stream.of(firstResult, secondResult)
                        .filter(UserResponse.class::isInstance)
                        .count();

        long duplicateFailures =
                Stream.of(firstResult, secondResult)
                        .filter(EmailIsTakenException.class::isInstance)
                        .count();

        assertThat(successfulRegistrations).isEqualTo(1);
        assertThat(duplicateFailures).isEqualTo(1);
        assertThat(saveAttempts.get()).isEqualTo(2);
    }
}