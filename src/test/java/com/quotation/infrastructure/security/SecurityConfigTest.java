package com.quotation.infrastructure.security;

import com.quotation.application.port.in.ProductUseCase;
import com.quotation.application.port.in.QuotationUseCase;
import com.quotation.application.port.in.UserUseCase;
import com.quotation.application.port.in.AuthUseCase;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

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
    void unauthenticatedRequest_WhenAccessingProtectedEndpoint_ThenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/quotations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleClient_WhenPostProduct_ThenReturns403() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, List.of("ROLE_CLIENT"));

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleAdmin_WhenGetProducts_ThenReturns200OrOther() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 401 && status != 403 : "Expected access but got " + status;
                });
    }

    @Test
    void publicEndpoint_WhenLoginPath_ThenNotBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@test.com\",\"password\":\"Test1234!\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 401 && status != 403 : "Auth endpoint should be public but got " + status;
                });
    }
}
