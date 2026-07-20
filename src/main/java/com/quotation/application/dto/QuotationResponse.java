package com.quotation.application.dto;

import com.quotation.domain.enums.QuotationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full quotation data returned by the API")
public class QuotationResponse {

    @Schema(description = "Unique quotation identifier", example = "1")
    private Long id;

    @Schema(description = "Human-readable quotation number", example = "Q-2026-A1B2C3D4")
    private String quotationNumber;

    @Schema(description = "ID of the client", example = "1")
    private Long userId;

    @Schema(description = "Full name of the client", example = "John Doe")
    private String userName;

    @Schema(description = "Email of the client", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Current status of the quotation", example = "DRAFT")
    private QuotationStatus status;

    @Schema(description = "Date the quotation was created", example = "2026-05-27")
    private LocalDate createdAt;

    @Schema(description = "Date the quotation expires (createdAt + validityDays)", example = "2026-06-26")
    private LocalDate expiryDate;

    @Schema(description = "Number of days the quotation is valid", example = "30")
    private Integer validityDays;

    @Schema(description = "Sum of all line totals before quotation-level discount", example = "850.00")
    private BigDecimal subtotal;

    @Schema(description = "Quotation-level discount amount", example = "0.00")
    private BigDecimal discountAmount;

    @Schema(description = "Final total: subtotal − discountAmount", example = "850.00")
    private BigDecimal total;

    @Schema(description = "Internal notes", example = "Urgent order")
    private String notes;

    @Schema(description = "List of calculated line items")
    private List<QuotationLineResponse> lines;
}
