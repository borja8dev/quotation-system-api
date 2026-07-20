package com.quotation.infrastructure.input.rest;

import com.quotation.application.dto.AuthResponse;
import com.quotation.application.dto.LoginRequest;
import com.quotation.application.dto.RefreshRequest;
import com.quotation.application.dto.RegisterRequest;
import com.quotation.application.port.in.AuthUseCase;
import com.quotation.domain.exception.InvalidCredentialsException;
import com.quotation.domain.exception.InvalidRefreshTokenException;
import com.quotation.infrastructure.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthUseCase authUseCase;

    @Test
    void login_WhenValidCredentials_ThenReturns200WithTokens() throws Exception {
        when(authUseCase.login(any(LoginRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_WhenInvalidCredentials_ThenReturns401() throws Exception {
        when(authUseCase.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_WhenInvalidEmailFormat_ThenReturns400() throws Exception {
        LoginRequest bad = LoginRequest.builder().email("not-an-email").password("Test1234!").build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void refresh_WhenValidToken_ThenReturns200WithNewTokens() throws Exception {
        when(authUseCase.refresh(any(RefreshRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("valid-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void refresh_WhenExpiredToken_ThenReturns401() throws Exception {
        when(authUseCase.refresh(any(RefreshRequest.class))).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("expired-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void logout_WhenAuthenticated_ThenReturns204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNoContent());
    }

    private LoginRequest validLogin() {
        return LoginRequest.builder().email("user@test.com").password("Test1234!").build();
    }

    private AuthResponse authResponse() {
        return AuthResponse.builder()
                .accessToken("eyJ.access.token")
                .refreshToken("uuid-refresh-token")
                .expiresIn(900000L)
                .role("ROLE_CLIENT")
                .build();
    }
}
