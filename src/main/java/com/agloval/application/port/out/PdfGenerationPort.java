package com.agloval.application.port.out;

import com.agloval.application.dto.QuotationResponse;

public interface PdfGenerationPort {

    byte[] generateQuotationPdf(QuotationResponse quotation);
}
