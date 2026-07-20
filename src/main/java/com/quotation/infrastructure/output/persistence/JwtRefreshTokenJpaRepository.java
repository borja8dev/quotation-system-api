package com.quotation.infrastructure.output.persistence;

import com.quotation.domain.entity.JwtRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtRefreshTokenJpaRepository extends JpaRepository<JwtRefreshToken, Long> {

    Optional<JwtRefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);
}
