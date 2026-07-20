package com.quotation.domain.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ValidityCalculatorTest {

    private final ValidityCalculator calculator = new ValidityCalculator();

    @Test
    void calculateValidityDays_WhenSummerNonRegular_ThenThirtyDays() {
        LocalDate summerDate = LocalDate.of(2025, 7, 15);

        int days = calculator.calculateValidityDays(false, summerDate);

        assertThat(days).isEqualTo(30);
    }

    @Test
    void calculateValidityDays_WhenSummerRegular_ThenSixtyDays() {
        LocalDate summerDate = LocalDate.of(2025, 8, 1);

        int days = calculator.calculateValidityDays(true, summerDate);

        assertThat(days).isEqualTo(60);
    }

    @Test
    void calculateValidityDays_WhenDecemberNonRegular_ThenThirtyDays() {
        LocalDate decemberDate = LocalDate.of(2025, 12, 20);

        int days = calculator.calculateValidityDays(false, decemberDate);

        assertThat(days).isEqualTo(30);
    }

    @Test
    void calculateValidityDays_WhenDecemberRegular_ThenSixtyDays() {
        LocalDate decemberDate = LocalDate.of(2025, 12, 5);

        int days = calculator.calculateValidityDays(true, decemberDate);

        assertThat(days).isEqualTo(60);
    }

    @Test
    void calculateValidityDays_WhenNormalMonthNonRegular_ThenFortyFiveDays() {
        LocalDate normalDate = LocalDate.of(2025, 3, 10);

        int days = calculator.calculateValidityDays(false, normalDate);

        assertThat(days).isEqualTo(45);
    }

    @Test
    void calculateValidityDays_WhenNormalMonthRegular_ThenNinetyDays() {
        LocalDate normalDate = LocalDate.of(2025, 10, 1);

        int days = calculator.calculateValidityDays(true, normalDate);

        assertThat(days).isEqualTo(90);
    }
}
