package com.quotation.domain.exception;

public class QuotationNotFoundException extends RuntimeException {

    public QuotationNotFoundException(Long id) {
        super("Quotation not found with id: " + id);
    }

    public QuotationNotFoundException(String quotationNumber) {
        super("Quotation not found with number: " + quotationNumber);
    }
}