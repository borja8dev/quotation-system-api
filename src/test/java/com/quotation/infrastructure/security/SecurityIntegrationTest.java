package com.quotation.infrastructure.security;

import com.quotation.application.port.in.AuthUseCase;
import com.quotation.application.port.in.ProductUseCase;
import com.quotation.application.port.in.QuotationUseCase;
import com.quotation.application.port.in.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private ProductUseCase productUseCase;

    @MockBean
    private QuotationUseCase quotationUseCase;

    @MockBean
    private AuthUseCase authUseCase;

    @Test
    void accessWithValidToken_WhenTokenIsValid_ThenNotBlocked() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, List.of("ROLE_CLIENT"));

        mockMvc.perform(get("/api/v1/quotations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 401 : "Valid token should pass auth, got " + status;
                });
    }

    @Test
    void accessWithTamperedToken_WhenTokenIsInvalid_ThenReturns401() throws Exception {
        String valid = jwtTokenProvider.generateAccessToken(1L, List.of("ROLE_CLIENT"));
        String tampered = valid.substring(0, valid.length() - 5) + "XXXXX";

        mockMvc.perform(get("/api/v1/quotations")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void noToken_WhenAccessingProtected_ThenReturns401WithJson() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rateLimiting_WhenExceedingLoginAttempts_ThenReturns429() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType("application/json")
                    .content("{\"email\":\"test@test.com\",\"password\":\"Test1234!\"}"));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@test.com\",\"password\":\"Test1234!\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void swaggerEndpoint_WhenAccessed_ThenIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 401 && status != 403 : "Swagger should be public, got " + status;
                });
    }
}
