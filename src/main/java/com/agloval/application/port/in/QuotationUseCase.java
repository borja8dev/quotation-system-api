package com.agloval.application.port.in;

import com.agloval.application.dto.QuotationRequest;
import com.agloval.application.dto.QuotationResponse;
import com.agloval.domain.enums.QuotationStatus;

import java.util.List;

public interface QuotationUseCase {

    QuotationResponse createQuotation(QuotationRequest request);

    QuotationResponse getQuotationById(Long id);

    List<QuotationResponse> getAllQuotations();

    QuotationResponse updateStatus(Long id, QuotationStatus status);

    List<QuotationResponse> getQuotationsByUserId(Long userId);

    byte[] getQuotationPdf(Long id);
}
