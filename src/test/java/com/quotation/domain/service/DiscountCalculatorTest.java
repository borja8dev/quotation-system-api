package com.quotation.domain.service;

import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.QuotationLine;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.SaleUnit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountCalculatorTest {

    private final DiscountCalculator calculator = new DiscountCalculator();

    @Test
    void calculateLineDiscount_WhenBoardCountBelow24_ThenNoVolumeDiscount() {
        Product board = boardProduct(18).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 10, false);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateLineDiscount_WhenBoardCount24_ThenThreePercent() {
        Product board = boardProduct(18).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 24, false);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    void calculateLineDiscount_WhenBoardCount47_ThenThreePercent() {
        Product board = boardProduct(18).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 47, false);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    void calculateLineDiscount_WhenBoardCount48_ThenSixPercent() {
        Product board = boardProduct(18).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 48, false);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("6"));
    }

    @Test
    void calculateLineDiscount_WhenBoardCount100_ThenSixPercent() {
        Product board = boardProduct(18).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 100, false);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("6"));
    }

    @Test
    void calculateLineDiscount_When16mmBoard_ThenAddsThreePercentBonus() {
        Product board = boardProduct(16).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 10, false);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    void calculateLineDiscount_WhenNon16mmBoard_ThenNoThicknessBonus() {
        Product board = boardProduct(18).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 10, false);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateLineDiscount_WhenRegularCustomer_ThenAddsTwoPercent() {
        Product board = boardProduct(18).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 10, true);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void calculateLineDiscount_WhenAllDiscountsStack_ThenElevenPercent() {
        Product board = boardProduct(16).build();

        BigDecimal discount = calculator.calculateLineDiscount(board, 48, true);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("11"));
    }

    @Test
    void calculateLineDiscount_WhenNonBoardProduct_ThenNoVolumeOrThicknessDiscount() {
        Product service = serviceProduct().build();

        BigDecimal discount = calculator.calculateLineDiscount(service, 100, false);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateLineDiscount_WhenNonBoardProductRegularCustomer_ThenOnlyTwoPercent() {
        Product service = serviceProduct().build();

        BigDecimal discount = calculator.calculateLineDiscount(service, 100, true);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void countTotalBoards_WhenMixedProducts_ThenCountsOnlyBoards() {
        Product board = boardProduct(18).build();
        Product service = serviceProduct().build();

        List<QuotationLine> lines = List.of(
                QuotationLine.builder().product(board).quantity(new BigDecimal("10")).build(),
                QuotationLine.builder().product(service).quantity(new BigDecimal("5")).build(),
                QuotationLine.builder().product(board).quantity(new BigDecimal("14")).build()
        );

        int count = calculator.countTotalBoards(lines);

        assertThat(count).isEqualTo(24);
    }

    @Test
    void buildDiscountBreakdown_WhenNoDiscounts_ThenReturnsNull() {
        Product board = boardProduct(18).build();

        String breakdown = calculator.buildDiscountBreakdown(board, 10, false);

        assertThat(breakdown).isNull();
    }

    @Test
    void buildDiscountBreakdown_WhenAllDiscounts_ThenFormatsCorrectly() {
        Product board = boardProduct(16).build();

        String breakdown = calculator.buildDiscountBreakdown(board, 48, true);

        assertThat(breakdown).contains("Volume 6% (48+ boards)");
        assertThat(breakdown).contains("16mm bonus 3%");
        assertThat(breakdown).contains("Regular customer 2%");
        assertThat(breakdown).contains("= 11%");
    }

    private Product.ProductBuilder boardProduct(int thicknessMm) {
        return Product.builder()
                .id(1L)
                .name("Oak Board " + thicknessMm + "mm")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .thicknessMm(thicknessMm)
                .pricePerUnit(new BigDecimal("45.00"));
    }

    private Product.ProductBuilder serviceProduct() {
        return Product.builder()
                .id(2L)
                .name("Installation Service")
                .category(ProductCategory.SERVICIO)
                .saleUnit(SaleUnit.MINUTO)
                .pricePerRateUnit(new BigDecimal("12.00"));
    }
}
