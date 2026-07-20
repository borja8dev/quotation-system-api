package com.quotation.application.dto;

import com.quotation.domain.entity.QuotationLine;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CalculationResult {
    private final List<QuotationLine> lines;
    private final BigDecimal subtotal;
    private final BigDecimal discountAmount;
    private final BigDecimal total;
    private final int validityDays;
}
