package com.quotation.application.service;

import com.quotation.application.dto.CalculationResult;
import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.QuotationLine;
import com.quotation.domain.service.DiscountCalculator;
import com.quotation.domain.service.PricingCalculator;
import com.quotation.domain.service.QuotationValidator;
import com.quotation.domain.service.ValidityCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class QuotationCalculationService {

    private final PricingCalculator pricingCalculator = new PricingCalculator();
    private final DiscountCalculator discountCalculator = new DiscountCalculator();
    private final QuotationValidator quotationValidator = new QuotationValidator();
    private final ValidityCalculator validityCalculator = new ValidityCalculator();

    /**
     * Calculates all line totals, discounts, and quotation totals.
     *
     * @param lines             The quotation lines (must have product set on each)
     * @param isRegularCustomer Whether the customer is a regular
     * @param quotationDate     The date for validity calculation
     * @return CalculationResult with calculated lines, subtotal, discountAmount, total, validityDays
     */
    public CalculationResult calculate(List<QuotationLine> lines, boolean isRegularCustomer, LocalDate quotationDate) {
        quotationValidator.validate(lines);

        int totalBoardCount = discountCalculator.countTotalBoards(lines);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        for (QuotationLine line : lines) {
            Product product = line.getProduct();

            BigDecimal unitPrice = pricingCalculator.resolveUnitPrice(product);
            BigDecimal discountPercent = discountCalculator.calculateLineDiscount(product, totalBoardCount, isRegularCustomer);
            String breakdown = discountCalculator.buildDiscountBreakdown(product, totalBoardCount, isRegularCustomer);

            BigDecimal discountFactor = BigDecimal.ONE.subtract(
                    discountPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            BigDecimal grossLineTotal = line.getQuantity().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = line.getQuantity().multiply(unitPrice).multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineDiscountAmount = grossLineTotal.subtract(lineTotal);

            line.setUnitPrice(unitPrice);
            line.setDiscountPercent(discountPercent);
            line.setLineTotal(lineTotal);
            line.setDiscountBreakdown(breakdown);

            subtotal = subtotal.add(grossLineTotal);
            totalDiscountAmount = totalDiscountAmount.add(lineDiscountAmount);
        }

        BigDecimal total = subtotal.subtract(totalDiscountAmount);
        int validityDays = validityCalculator.calculateValidityDays(isRegularCustomer, quotationDate);

        return new CalculationResult(lines, subtotal, totalDiscountAmount, total, validityDays);
    }
}
