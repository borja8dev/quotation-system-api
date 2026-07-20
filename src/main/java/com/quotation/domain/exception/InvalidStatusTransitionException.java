package com.quotation.domain.exception;

import com.quotation.domain.enums.QuotationStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(QuotationStatus from, QuotationStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
