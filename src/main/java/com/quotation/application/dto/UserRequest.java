package com.quotation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating or updating a user")
public class UserRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Full name of the user", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address — must be unique", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Phone is required")
    @Schema(description = "Contact phone number", example = "600123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Company name (optional)", example = "Reformas García SL")
    private String companyName;

    @Schema(description = "Whether this user is a regular/recurring client", example = "false")
    private boolean regular;
}
