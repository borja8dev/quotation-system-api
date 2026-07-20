package com.quotation.application.port.out;

import com.quotation.domain.entity.JwtRefreshToken;

import java.util.Optional;

public interface JwtRefreshTokenRepositoryPort {

    JwtRefreshToken save(JwtRefreshToken token);

    Optional<JwtRefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);

    void delete(JwtRefreshToken token);
}
