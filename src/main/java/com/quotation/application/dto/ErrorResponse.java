package com.quotation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response returned on all failed requests")
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp of the error", example = "2026-05-27T19:05:28")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP status description", example = "Not Found")
    private String error;

    @Schema(description = "Human-readable error message", example = "User not found with id: 99")
    private String message;

    @Schema(description = "Request path that triggered the error", example = "/api/v1/users/99")
    private String path;

    @Schema(description = "Field-level validation errors — only present on 400 responses",
            example = "[\"name: Name is required\", \"email: Email must be valid\"]")
    private List<String> validationErrors;
}
