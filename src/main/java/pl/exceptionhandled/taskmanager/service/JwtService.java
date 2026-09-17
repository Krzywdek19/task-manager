package pl.exceptionhandled.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import pl.exceptionhandled.taskmanager.security.GeneratedToken;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public GeneratedToken generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(1));

        var authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("roles", authorities)
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue();

        return new GeneratedToken(token, expiresAt);
    }
}