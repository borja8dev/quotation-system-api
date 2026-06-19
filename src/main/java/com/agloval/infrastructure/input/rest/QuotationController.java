package com.agloval.infrastructure.input.rest;

import com.agloval.application.dto.ErrorResponse;
import com.agloval.application.dto.QuotationRequest;
import com.agloval.application.dto.QuotationResponse;
import com.agloval.application.port.in.QuotationUseCase;
import com.agloval.domain.enums.QuotationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
@Tag(name = "Quotations", description = "Create and manage quotations — each quotation groups line items and calculates totals automatically")
public class QuotationController {

    private final QuotationUseCase quotationUseCase;

    @PostMapping
    @Operation(
            summary = "Create a new quotation",
            description = "Creates a quotation for a given client. Each line item's total is calculated as: " +
                    "quantity × unitPrice × (1 − discountPercent / 100). " +
                    "The quotation subtotal is the sum of all line totals. A unique quotation number is auto-generated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quotation created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuotationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input — validation errors",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or product not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuotationResponse> createQuotation(@Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quotationUseCase.createQuotation(request));
    }

    @GetMapping
    @Operation(summary = "List all quotations", description = "Returns all quotations in the system regardless of status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of quotations",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = QuotationResponse.class))))
    })
    public ResponseEntity<List<QuotationResponse>> getAllQuotations() {
        return ResponseEntity.ok(quotationUseCase.getAllQuotations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quotation by ID", description = "Returns a full quotation including all line items and calculated totals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quotation found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuotationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quotation not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuotationResponse> getQuotationById(
            @Parameter(description = "Numeric ID of the quotation", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(quotationUseCase.getQuotationById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update quotation status",
            description = "Transitions a quotation to a new status. " +
                    "Valid values: DRAFT → SENT → CONFIRMED → DONE or ARCHIVED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuotationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quotation not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuotationResponse> updateStatus(
            @Parameter(description = "Numeric ID of the quotation", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "New status to apply", example = "SENT", required = true)
            @RequestParam QuotationStatus status) {
        return ResponseEntity.ok(quotationUseCase.updateStatus(id, status));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Download quotation as PDF",
            description = "Generates and returns a PDF document for the given quotation, " +
                    "including client details, line items with discount breakdowns, VAT (21%), and validity footer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF file generated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
            @ApiResponse(responseCode = "404", description = "Quotation not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<byte[]> downloadPdf(
            @Parameter(description = "Numeric ID of the quotation", example = "1", required = true)
            @PathVariable Long id) {
        byte[] pdf = quotationUseCase.getQuotationPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("presupuesto-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get quotations by user", description = "Returns all quotations associated with a specific client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of quotations for the user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = QuotationResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<QuotationResponse>> getQuotationsByUser(
            @Parameter(description = "Numeric ID of the user", example = "1", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(quotationUseCase.getQuotationsByUserId(userId));
    }
}
