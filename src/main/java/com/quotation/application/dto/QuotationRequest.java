package com.quotation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new quotation")
public class QuotationRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the client this quotation is for", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "Number of days the quotation remains valid. Defaults to 30.", example = "30")
    private Integer validityDays;

    @Schema(description = "Optional internal notes for this quotation", example = "Urgent order — client confirmed delivery by end of month")
    private String notes;

    @NotEmpty(message = "Quotation must have at least one line")
    @Valid
    @Schema(description = "List of line items — at least one required", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<QuotationLineRequest> lines;
}
