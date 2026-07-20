package com.quotation.infrastructure.output.pdf;

import com.quotation.application.dto.QuotationLineResponse;
import com.quotation.application.dto.QuotationResponse;
import com.quotation.domain.enums.QuotationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfAdapterTest {

    private final OpenPdfAdapter adapter = new OpenPdfAdapter();

    @Test
    void generateQuotationPdf_ReturnsNonEmptyBytes() {
        byte[] pdf = adapter.generateQuotationPdf(sampleQuotation());

        assertThat(pdf).isNotNull().isNotEmpty();
    }

    @Test
    void generateQuotationPdf_StartsWithPdfMagicNumber() {
        byte[] pdf = adapter.generateQuotationPdf(sampleQuotation());

        String header = new String(pdf, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    void generateQuotationPdf_HandlesNullDiscountBreakdown() {
        QuotationResponse quotation = sampleQuotation();
        quotation.getLines().get(0).setDiscountBreakdown(null);

        byte[] pdf = adapter.generateQuotationPdf(quotation);

        assertThat(pdf).isNotEmpty();
    }

    private QuotationResponse sampleQuotation() {
        QuotationLineResponse line = QuotationLineResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("Tablero MDF 16mm")
                .quantity(new BigDecimal("10"))
                .unitPrice(new BigDecimal("25.00"))
                .discountPercent(new BigDecimal("9.00"))
                .lineTotal(new BigDecimal("227.50"))
                .description("Color blanco")
                .discountBreakdown("Volume 6% + Regular 3% = 9%")
                .build();

        return QuotationResponse.builder()
                .id(1L)
                .quotationNumber("Q-2026-ABCD1234")
                .userId(1L)
                .userName("John Doe")
                .userEmail("john@example.com")
                .status(QuotationStatus.DRAFT)
                .createdAt(LocalDate.of(2026, 6, 19))
                .expiryDate(LocalDate.of(2026, 7, 19))
                .validityDays(30)
                .subtotal(new BigDecimal("250.00"))
                .discountAmount(new BigDecimal("22.50"))
                .total(new BigDecimal("227.50"))
                .notes("Test quotation")
                .lines(List.of(line))
                .build();
    }
}
