package com.quotation.infrastructure.input.rest;

import com.quotation.application.dto.ProductRequest;
import com.quotation.application.dto.ProductResponse;
import com.quotation.application.port.in.ProductUseCase;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.SaleUnit;
import com.quotation.domain.exception.ProductNotFoundException;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = ProductController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductUseCase productUseCase;

    @Test
    void createProduct_WhenValidRequest_ThenReturns201() throws Exception {
        when(productUseCase.createProduct(any(ProductRequest.class))).thenReturn(productResponse());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Oak Tablero 18mm"));
    }

    @Test
    void createProduct_WhenMissingRequiredFields_ThenReturns400() throws Exception {
        ProductRequest invalid = ProductRequest.builder().build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void getProductById_WhenProductExists_ThenReturns200() throws Exception {
        when(productUseCase.getProductById(1L)).thenReturn(productResponse());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("TABLERO"));
    }

    @Test
    void getProductById_WhenProductNotFound_ThenReturns404() throws Exception {
        when(productUseCase.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllProducts_WhenProductsExist_ThenReturns200WithList() throws Exception {
        when(productUseCase.getAllProducts()).thenReturn(List.of(productResponse()));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private ProductRequest validRequest() {
        return ProductRequest.builder()
                .name("Oak Tablero 18mm")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .pricePerUnit(BigDecimal.valueOf(45.00))
                .build();
    }

    private ProductResponse productResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("Oak Tablero 18mm")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .pricePerUnit(BigDecimal.valueOf(45.00))
                .build();
    }
}
