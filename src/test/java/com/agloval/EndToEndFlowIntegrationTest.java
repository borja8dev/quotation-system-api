package com.agloval;

import com.agloval.application.port.out.ProductRepositoryPort;
import com.agloval.domain.entity.Product;
import com.agloval.domain.enums.ProductCategory;
import com.agloval.domain.enums.SaleUnit;
import com.agloval.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EndToEndFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepositoryPort productRepositoryPort;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private Long seededProductId;

    @BeforeEach
    void setUp() {
        Product board = productRepositoryPort.save(Product.builder()
                .name("Pine Board 18mm 244x122")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .widthCm(244)
                .lengthCm(122)
                .thicknessMm(18)
                .pricePerUnit(new BigDecimal("45.00"))
                .build());
        seededProductId = board.getId();
    }

    @Test
    void fullFlow_WhenRegisterCreateQuotationDownloadPdf_ThenSucceeds() throws Exception {
        // Step 1: Register a new user and obtain an access token
        String registerBody = """
                {"name":"E2E Test User","email":"e2e.flow@agloval.test","password":"Test1234!"}
                """;

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String accessToken = objectMapper
                .readTree(registerResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        // Step 2: Create a quotation using the authenticated user
        String quotationBody = """
                {
                  "userId": %d,
                  "lines": [{"productId": %d, "quantity": 10}]
                }
                """.formatted(userId, seededProductId);

        MvcResult quotationResult = mockMvc.perform(post("/api/v1/quotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(quotationBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.quotationNumber", startsWith("Q-")))
                .andExpect(jsonPath("$.total").isNumber())
                .andReturn();

        Long quotationId = objectMapper
                .readTree(quotationResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Step 3: Download the quotation as PDF
        byte[] pdf = mockMvc.perform(get("/api/v1/quotations/{id}/pdf", quotationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition",
                        containsString("presupuesto-" + quotationId + ".pdf")))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void createQuotation_WhenNoToken_ThenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/quotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"lines\":[{\"productId\":1,\"quantity\":1}]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void downloadPdf_WhenNoToken_ThenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/quotations/1/pdf")
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isUnauthorized());
    }
}
