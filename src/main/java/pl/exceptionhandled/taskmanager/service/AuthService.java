package pl.exceptionhandled.taskmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.exceptionhandled.taskmanager.dto.*;
import pl.exceptionhandled.taskmanager.entity.Role;
import pl.exceptionhandled.taskmanager.entity.User;
import pl.exceptionhandled.taskmanager.exception.EmailIsTakenException;
import pl.exceptionhandled.taskmanager.exception.InvalidCredentialsException;
import pl.exceptionhandled.taskmanager.mapper.UserMapper;
import pl.exceptionhandled.taskmanager.repository.UserRepository;
import pl.exceptionhandled.taskmanager.security.GeneratedToken;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(email)) {
            throw new EmailIsTakenException(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(Role.USER);

        try {
            User savedUser = repository.saveAndFlush(user);
            return mapper.userToResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            if (isEmailConstraintViolation(exception)) {
                throw new EmailIsTakenException(email);
            }

            throw exception;
        }
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticate(request);

            GeneratedToken token = jwtService.generateToken(authentication);

            return new LoginResponse(
                    token.value(),
                    token.expiresAt()
            );
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }
    }

    private Authentication authenticate(LoginRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        var authenticationToken =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        email,
                        request.password()
                );

        return authenticationManager.authenticate(authenticationToken);
    }

    private boolean isEmailConstraintViolation(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException violation) {
                return "uq_users_email".equals(violation.getConstraintName());
            }

            current = current.getCause();
        }

        return false;
    }
}