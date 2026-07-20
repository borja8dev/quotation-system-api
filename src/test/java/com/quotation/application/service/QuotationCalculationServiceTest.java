package com.quotation.application.service;

import com.quotation.application.dto.CalculationResult;
import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.QuotationLine;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.RateType;
import com.quotation.domain.enums.SaleUnit;
import com.quotation.domain.exception.QuotationValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuotationCalculationServiceTest {

    private final QuotationCalculationService service = new QuotationCalculationService();

    // January — standard season, non-regular → validityDays = 45
    private static final LocalDate STANDARD_DATE = LocalDate.of(2025, 1, 15);
    // July — summer season, non-regular → validityDays = 30
    private static final LocalDate SUMMER_DATE = LocalDate.of(2025, 7, 15);

    @Test
    void calculate_WhenSingleBoardLine_ThenCalculatesCorrectly() {
        List<QuotationLine> lines = List.of(lineOf(boardProduct(18), "1"));

        CalculationResult result = service.calculate(lines, false, STANDARD_DATE);

        QuotationLine line = result.getLines().get(0);
        assertThat(line.getUnitPrice()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(line.getDiscountPercent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("45.00"));

        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(result.getValidityDays()).isEqualTo(45);
    }

    @Test
    void calculate_WhenVolumeDiscountApplies_ThenReducesTotal() {
        // 24 boards → 3% volume discount
        List<QuotationLine> lines = List.of(lineOf(boardProduct(18), "24"));

        CalculationResult result = service.calculate(lines, false, STANDARD_DATE);

        // gross = 24 * 45 = 1080.00; lineTotal = 1080 * 0.97 = 1047.60; discountAmount = 32.40
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("1080.00"));
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("32.40"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("1047.60"));

        QuotationLine line = result.getLines().get(0);
        assertThat(line.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(line.getLineTotal()).isEqualByComparingTo(new BigDecimal("1047.60"));
    }

    @Test
    void calculate_WhenHighVolumeDiscount_ThenSixPercent() {
        // 48 boards → 6% volume discount
        List<QuotationLine> lines = List.of(lineOf(boardProduct(18), "48"));

        CalculationResult result = service.calculate(lines, false, STANDARD_DATE);

        // gross = 48 * 45 = 2160.00; lineTotal = 2160 * 0.94 = 2030.40; discountAmount = 129.60
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("2160.00"));
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("129.60"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("2030.40"));

        QuotationLine line = result.getLines().get(0);
        assertThat(line.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("6"));
    }

    @Test
    void calculate_When16mmBoardsWithVolume_ThenStacksDiscounts() {
        // 48 boards × 16mm → 6% volume + 3% thickness = 9% total
        List<QuotationLine> lines = List.of(lineOf(boardProduct(16), "48"));

        CalculationResult result = service.calculate(lines, false, STANDARD_DATE);

        // gross = 2160.00; lineTotal = 2160 * 0.91 = 1965.60; discountAmount = 194.40
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("2160.00"));
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("194.40"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("1965.60"));

        QuotationLine line = result.getLines().get(0);
        assertThat(line.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("9"));
        assertThat(line.getDiscountBreakdown()).contains("Volume 6%");
        assertThat(line.getDiscountBreakdown()).contains("16mm bonus 3%");
    }

    @Test
    void calculate_WhenRegularCustomerWithBoards_ThenStacksAllDiscounts() {
        // 48 boards × 16mm + regular customer → 6% + 3% + 2% = 11% total
        List<QuotationLine> lines = List.of(lineOf(boardProduct(16), "48"));

        CalculationResult result = service.calculate(lines, true, STANDARD_DATE);

        // gross = 2160.00; lineTotal = 2160 * 0.89 = 1922.40; discountAmount = 237.60
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("2160.00"));
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("237.60"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("1922.40"));

        QuotationLine line = result.getLines().get(0);
        assertThat(line.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("11"));
        assertThat(line.getDiscountBreakdown()).contains("Regular customer 2%");
    }

    @Test
    void calculate_WhenMixedProductTypes_ThenAppliesCorrectDiscounts() {
        // 24 boards (18mm) → 3% volume; 1 service line (30 min) → 0% discount; not regular
        QuotationLine boardLine = lineOf(boardProduct(18), "24");
        QuotationLine serviceLine = lineOf(serviceProduct(), "30");
        List<QuotationLine> lines = List.of(boardLine, serviceLine);

        CalculationResult result = service.calculate(lines, false, STANDARD_DATE);

        // Board: gross = 24*45 = 1080.00, discount 3% → lineTotal = 1047.60, discountAmt = 32.40
        // Service: gross = 30*12 = 360.00, discount 0% → lineTotal = 360.00, discountAmt = 0
        // subtotal = 1440.00, totalDiscount = 32.40, total = 1407.60
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("1440.00"));
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("32.40"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("1407.60"));

        QuotationLine calculatedBoard = result.getLines().get(0);
        assertThat(calculatedBoard.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("3"));

        QuotationLine calculatedService = result.getLines().get(1);
        assertThat(calculatedService.getDiscountPercent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(calculatedService.getDiscountBreakdown()).isNull();
    }

    @Test
    void calculate_WhenInvalidLines_ThenThrowsValidationException() {
        // zero quantity is invalid
        List<QuotationLine> lines = List.of(
                QuotationLine.builder()
                        .product(boardProduct(18))
                        .quantity(BigDecimal.ZERO)
                        .build()
        );

        assertThatThrownBy(() -> service.calculate(lines, false, STANDARD_DATE))
                .isInstanceOf(QuotationValidationException.class)
                .hasMessageContaining("quantity must be positive");
    }

    @Test
    void calculate_WhenSummerDate_ThenShorterValidity() {
        List<QuotationLine> lines = List.of(lineOf(boardProduct(18), "1"));

        CalculationResult result = service.calculate(lines, false, SUMMER_DATE);

        assertThat(result.getValidityDays()).isEqualTo(30);
    }

    // --- helpers ---

    private QuotationLine lineOf(Product product, String quantity) {
        return QuotationLine.builder()
                .product(product)
                .quantity(new BigDecimal(quantity))
                .build();
    }

    private Product boardProduct(int thicknessMm) {
        return Product.builder()
                .id(1L)
                .name("Oak Board " + thicknessMm + "mm")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .thicknessMm(thicknessMm)
                .pricePerUnit(new BigDecimal("45.00"))
                .build();
    }

    private Product serviceProduct() {
        return Product.builder()
                .id(2L)
                .name("Installation Service")
                .category(ProductCategory.SERVICIO)
                .saleUnit(SaleUnit.MINUTO)
                .rateType(RateType.PER_MINUTE)
                .pricePerRateUnit(new BigDecimal("12.00"))
                .build();
    }
}
