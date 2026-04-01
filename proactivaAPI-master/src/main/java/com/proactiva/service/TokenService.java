package com.proactiva.service;

import com.proactiva.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    public String generateToken(User user) {
        // Define o tempo de expiração do token (ex: 24 horas)
        long duration = Duration.ofHours(24).toSeconds();

        return Jwt.issuer("proactiva-issuer")
                .subject(user.getNomeCompleto())
                .upn(user.getEmail())
                .groups(Set.of("user")) // Define o grupo/role
                .expiresIn(duration)
                .claim("userId", user.getId()) // Adiciona o ID do usuário como claim
                .sign();
    }
}
