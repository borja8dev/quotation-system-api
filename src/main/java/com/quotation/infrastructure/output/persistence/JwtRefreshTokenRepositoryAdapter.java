package com.quotation.infrastructure.output.persistence;

import com.quotation.application.port.out.JwtRefreshTokenRepositoryPort;
import com.quotation.domain.entity.JwtRefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtRefreshTokenRepositoryAdapter implements JwtRefreshTokenRepositoryPort {

    private final JwtRefreshTokenJpaRepository jpaRepository;

    @Override
    public JwtRefreshToken save(JwtRefreshToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<JwtRefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void delete(JwtRefreshToken token) {
        jpaRepository.delete(token);
    }
}
