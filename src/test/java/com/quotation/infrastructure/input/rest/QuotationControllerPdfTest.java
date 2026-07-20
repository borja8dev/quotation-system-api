package com.quotation.infrastructure.input.rest;

import com.quotation.application.port.in.QuotationUseCase;
import com.quotation.domain.exception.QuotationNotFoundException;
import com.quotation.infrastructure.config.GlobalExceptionHandler;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = QuotationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class QuotationControllerPdfTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuotationUseCase quotationUseCase;

    @Test
    void getPdf_WhenExists_Returns200WithPdfContentType() throws Exception {
        byte[] fakePdf = "%PDF-1.4 fake content".getBytes();
        when(quotationUseCase.getQuotationPdf(1L)).thenReturn(fakePdf);

        mockMvc.perform(get("/api/v1/quotations/1/pdf")
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"presupuesto-1.pdf\""));
    }

    @Test
    void getPdf_WhenNotFound_Returns404() throws Exception {
        when(quotationUseCase.getQuotationPdf(999L)).thenThrow(new QuotationNotFoundException(999L));

        mockMvc.perform(get("/api/v1/quotations/999/pdf")
                        .accept(MediaType.APPLICATION_PDF, MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
