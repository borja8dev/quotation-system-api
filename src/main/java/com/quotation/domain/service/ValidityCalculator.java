package com.quotation.domain.service;

import java.time.LocalDate;

public class ValidityCalculator {

    private static final int SUMMER_START_MONTH = 6;
    private static final int SUMMER_END_MONTH = 8;
    private static final int HOLIDAY_MONTH = 12;
    private static final int SUMMER_VALIDITY_REGULAR = 60;
    private static final int SUMMER_VALIDITY_STANDARD = 30;
    private static final int STANDARD_VALIDITY_REGULAR = 90;
    private static final int STANDARD_VALIDITY_STANDARD = 45;

    public int calculateValidityDays(boolean isRegularCustomer, LocalDate date) {
        int month = date.getMonthValue();
        boolean isSummerOrHoliday = (month >= SUMMER_START_MONTH && month <= SUMMER_END_MONTH) || month == HOLIDAY_MONTH;

        if (isSummerOrHoliday) {
            return isRegularCustomer ? SUMMER_VALIDITY_REGULAR : SUMMER_VALIDITY_STANDARD;
        } else {
            return isRegularCustomer ? STANDARD_VALIDITY_REGULAR : STANDARD_VALIDITY_STANDARD;
        }
    }
}
