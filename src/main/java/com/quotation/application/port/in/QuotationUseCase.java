package com.quotation.application.port.in;

import com.quotation.application.dto.QuotationRequest;
import com.quotation.application.dto.QuotationResponse;
import com.quotation.domain.enums.QuotationStatus;

import java.util.List;

public interface QuotationUseCase {

    QuotationResponse createQuotation(QuotationRequest request);

    QuotationResponse getQuotationById(Long id);

    List<QuotationResponse> getAllQuotations();

    QuotationResponse updateStatus(Long id, QuotationStatus status);

    List<QuotationResponse> getQuotationsByUserId(Long userId);

    byte[] getQuotationPdf(Long id);
}
