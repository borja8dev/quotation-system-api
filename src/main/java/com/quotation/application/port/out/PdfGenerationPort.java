package com.quotation.application.port.out;

import com.quotation.application.dto.QuotationResponse;

public interface PdfGenerationPort {

    byte[] generateQuotationPdf(QuotationResponse quotation);
}
