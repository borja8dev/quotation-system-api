package com.quotation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User data returned by the API")
public class UserResponse {

    @Schema(description = "Unique user identifier", example = "1")
    private Long id;

    @Schema(description = "Full name", example = "John Doe")
    private String name;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Contact phone number", example = "600123456")
    private String phone;

    @Schema(description = "Company name", example = "Reformas García SL")
    private String companyName;

    @Schema(description = "Whether this is a regular/recurring client", example = "false")
    private boolean regular;
}
