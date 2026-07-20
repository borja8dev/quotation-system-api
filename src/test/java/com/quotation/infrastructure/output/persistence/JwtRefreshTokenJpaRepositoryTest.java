package com.quotation.infrastructure.output.persistence;

import com.quotation.domain.entity.JwtRefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JwtRefreshTokenJpaRepositoryTest {

    @Autowired
    private JwtRefreshTokenJpaRepository repository;

    @Test
    void save_WhenValidToken_ThenPersists() {
        JwtRefreshToken saved = repository.save(buildToken(1L));
        assertNotNull(saved.getId());
    }

    @Test
    void findByToken_WhenTokenExists_ThenReturnsIt() {
        String rawToken = UUID.randomUUID().toString();
        repository.save(buildTokenWithValue(1L, rawToken));

        Optional<JwtRefreshToken> found = repository.findByToken(rawToken);
        assertTrue(found.isPresent());
        assertEquals(rawToken, found.get().getToken());
    }

    @Test
    void findByToken_WhenTokenNotFound_ThenReturnsEmpty() {
        Optional<JwtRefreshToken> found = repository.findByToken("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void deleteByUserId_WhenTokensExist_ThenRemovesThem() {
        repository.save(buildToken(99L));
        repository.save(buildToken(99L));
        repository.deleteByUserId(99L);
        assertEquals(0, repository.findAll().stream()
                .filter(t -> t.getUserId().equals(99L)).count());
    }

    private JwtRefreshToken buildToken(Long userId) {
        return buildTokenWithValue(userId, UUID.randomUUID().toString());
    }

    private JwtRefreshToken buildTokenWithValue(Long userId, String token) {
        return JwtRefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
    }
}
