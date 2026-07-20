package com.quotation.domain.service;

import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.QuotationLine;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.RateType;
import com.quotation.domain.enums.SaleUnit;
import com.quotation.domain.exception.QuotationValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class QuotationValidatorTest {

    private final QuotationValidator validator = new QuotationValidator();

    @Test
    void validate_WhenValidBoard_ThenNoException() {
        List<QuotationLine> lines = List.of(
                QuotationLine.builder()
                        .product(validBoard().build())
                        .quantity(new BigDecimal("5"))
                        .build()
        );

        assertThatNoException().isThrownBy(() -> validator.validate(lines));
    }

    @Test
    void validate_WhenThicknessBelowMinimum_ThenThrows() {
        Product board = validBoard().thicknessMm(2).build();

        List<QuotationLine> lines = List.of(
                QuotationLine.builder().product(board).quantity(new BigDecimal("1")).build()
        );

        assertThatThrownBy(() -> validator.validate(lines))
                .isInstanceOf(QuotationValidationException.class)
                .satisfies(ex -> assertThat(((QuotationValidationException) ex).getViolations())
                        .anyMatch(v -> v.contains("thickness") && v.contains("2mm")));
    }

    @Test
    void validate_WhenThicknessAboveMaximum_ThenThrows() {
        Product board = validBoard().thicknessMm(50).build();

        List<QuotationLine> lines = List.of(
                QuotationLine.builder().product(board).quantity(new BigDecimal("1")).build()
        );

        assertThatThrownBy(() -> validator.validate(lines))
                .isInstanceOf(QuotationValidationException.class)
                .satisfies(ex -> assertThat(((QuotationValidationException) ex).getViolations())
                        .anyMatch(v -> v.contains("thickness") && v.contains("50mm")));
    }

    @Test
    void validate_WhenNonStandardDimensions_ThenThrows() {
        Product board = validBoard().widthCm(100).lengthCm(100).build();

        List<QuotationLine> lines = List.of(
                QuotationLine.builder().product(board).quantity(new BigDecimal("1")).build()
        );

        assertThatThrownBy(() -> validator.validate(lines))
                .isInstanceOf(QuotationValidationException.class)
                .satisfies(ex -> assertThat(((QuotationValidationException) ex).getViolations())
                        .anyMatch(v -> v.contains("dimensions") && v.contains("100x100")));
    }

    @Test
    void validate_WhenZeroQuantity_ThenThrows() {
        List<QuotationLine> lines = List.of(
                QuotationLine.builder()
                        .product(validBoard().build())
                        .quantity(BigDecimal.ZERO)
                        .build()
        );

        assertThatThrownBy(() -> validator.validate(lines))
                .isInstanceOf(QuotationValidationException.class)
                .satisfies(ex -> assertThat(((QuotationValidationException) ex).getViolations())
                        .anyMatch(v -> v.contains("quantity must be positive")));
    }

    @Test
    void validate_WhenNoPriceSet_ThenThrows() {
        Product noPriceBoard = Product.builder()
                .id(1L)
                .name("No Price Board")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .widthCm(244)
                .lengthCm(122)
                .thicknessMm(18)
                .build();

        List<QuotationLine> lines = List.of(
                QuotationLine.builder().product(noPriceBoard).quantity(new BigDecimal("1")).build()
        );

        assertThatThrownBy(() -> validator.validate(lines))
                .isInstanceOf(QuotationValidationException.class)
                .satisfies(ex -> assertThat(((QuotationValidationException) ex).getViolations())
                        .anyMatch(v -> v.contains("no price configured")));
    }

    @Test
    void validate_WhenValidServiceProduct_ThenNoException() {
        Product service = Product.builder()
                .id(2L)
                .name("Installation Service")
                .category(ProductCategory.SERVICIO)
                .saleUnit(SaleUnit.MINUTO)
                .rateType(RateType.PER_MINUTE)
                .pricePerRateUnit(new BigDecimal("12.00"))
                .build();

        List<QuotationLine> lines = List.of(
                QuotationLine.builder().product(service).quantity(new BigDecimal("30")).build()
        );

        assertThatNoException().isThrownBy(() -> validator.validate(lines));
    }

    @Test
    void validate_WhenMultipleViolations_ThenCollectsAll() {
        Product badBoard = Product.builder()
                .id(1L)
                .name("Bad Board")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .thicknessMm(2)
                .widthCm(100)
                .lengthCm(100)
                .build();

        List<QuotationLine> lines = List.of(
                QuotationLine.builder().product(badBoard).quantity(BigDecimal.ZERO).build()
        );

        assertThatThrownBy(() -> validator.validate(lines))
                .isInstanceOf(QuotationValidationException.class)
                .satisfies(ex -> {
                    List<String> violations = ((QuotationValidationException) ex).getViolations();
                    assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
                    assertThat(violations).anyMatch(v -> v.contains("quantity must be positive"));
                    assertThat(violations).anyMatch(v -> v.contains("no price configured"));
                    assertThat(violations).anyMatch(v -> v.contains("thickness"));
                });
    }

    private Product.ProductBuilder validBoard() {
        return Product.builder()
                .id(1L)
                .name("Oak Board 18mm")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .widthCm(244)
                .lengthCm(122)
                .thicknessMm(18)
                .pricePerUnit(new BigDecimal("45.00"));
    }
}
