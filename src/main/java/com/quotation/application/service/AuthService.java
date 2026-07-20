package com.quotation.application.service;

import com.quotation.application.dto.AuthResponse;
import com.quotation.application.dto.LoginRequest;
import com.quotation.application.dto.RefreshRequest;
import com.quotation.application.dto.RegisterRequest;
import com.quotation.application.port.in.AuthUseCase;
import com.quotation.application.port.out.JwtRefreshTokenRepositoryPort;
import com.quotation.application.port.out.UserRepositoryPort;
import com.quotation.domain.entity.JwtRefreshToken;
import com.quotation.domain.entity.User;
import com.quotation.domain.exception.DuplicateEmailException;
import com.quotation.domain.exception.InvalidCredentialsException;
import com.quotation.domain.exception.InvalidRefreshTokenException;
import com.quotation.domain.exception.PasswordValidationException;
import com.quotation.infrastructure.security.JwtTokenProvider;
import com.quotation.infrastructure.security.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final JwtRefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        if (!passwordValidator.isValid(request.getPassword())) {
            throw new PasswordValidationException();
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone() != null ? request.getPhone() : "")
                .companyName(request.getCompanyName())
                .role("ROLE_CLIENT")
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        JwtRefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked() || stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        refreshTokenRepository.delete(stored);
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), List.of(user.getRole()));

        String rawRefresh = UUID.randomUUID().toString();
        JwtRefreshToken refreshToken = JwtRefreshToken.builder()
                .token(rawRefresh)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenExpiry * 1_000_000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .expiresIn(accessTokenExpiry)
                .role(user.getRole())
                .build();
    }
}
