package com.quotation.domain.exception;

import java.util.List;

public class QuotationValidationException extends RuntimeException {

    private final List<String> violations;

    public QuotationValidationException(List<String> violations) {
        super("Quotation validation failed: " + String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    public List<String> getViolations() {
        return violations;
    }
}
