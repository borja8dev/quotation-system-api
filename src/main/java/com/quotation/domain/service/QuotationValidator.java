package com.quotation.domain.service;

import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.QuotationLine;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.exception.QuotationValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class QuotationValidator {

    private static final int MIN_THICKNESS_MM = 4;
    private static final int MAX_THICKNESS_MM = 40;

    private static final Set<String> STANDARD_BOARD_DIMENSIONS = Set.of(
            "244x122", "366x122", "305x122", "280x122", "260x122"
    );

    public void validate(List<QuotationLine> lines) {
        List<String> violations = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            QuotationLine line = lines.get(i);
            Product product = line.getProduct();
            int lineNumber = i + 1;

            if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                violations.add("Line " + lineNumber + ": quantity must be positive");
            }

            if (hasNoPrice(product)) {
                violations.add("Line " + lineNumber + ": product '" + product.getName() + "' has no price configured");
            }

            if (product.getCategory() == ProductCategory.TABLERO) {
                validateBoardDimensions(product, lineNumber, violations);
            }
        }

        if (!violations.isEmpty()) {
            throw new QuotationValidationException(violations);
        }
    }

    private void validateBoardDimensions(Product product, int lineNumber, List<String> violations) {
        if (product.getThicknessMm() != null) {
            if (product.getThicknessMm() < MIN_THICKNESS_MM || product.getThicknessMm() > MAX_THICKNESS_MM) {
                violations.add("Line " + lineNumber + ": board thickness " + product.getThicknessMm()
                        + "mm is outside valid range (" + MIN_THICKNESS_MM + "-" + MAX_THICKNESS_MM + "mm)");
            }
        }

        if (product.getWidthCm() != null && product.getLengthCm() != null) {
            String dims = product.getWidthCm() + "x" + product.getLengthCm();
            if (!STANDARD_BOARD_DIMENSIONS.contains(dims)) {
                violations.add("Line " + lineNumber + ": board dimensions " + dims + "cm are not standard");
            }
        }
    }

    private boolean hasNoPrice(Product product) {
        return product.getPricePerUnit() == null
                && product.getPricePerM2() == null
                && product.getPricePerRateUnit() == null;
    }
}
