package com.quotation.application.service;

import com.quotation.application.dto.CalculationResult;
import com.quotation.application.dto.QuotationLineRequest;
import com.quotation.application.dto.QuotationLineResponse;
import com.quotation.application.dto.QuotationRequest;
import com.quotation.application.dto.QuotationResponse;
import com.quotation.application.port.in.QuotationUseCase;
import com.quotation.application.port.out.PdfGenerationPort;
import com.quotation.application.port.out.ProductRepositoryPort;
import com.quotation.application.port.out.QuotationRepositoryPort;
import com.quotation.application.port.out.UserRepositoryPort;
import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.Quotation;
import com.quotation.domain.entity.QuotationLine;
import com.quotation.domain.entity.User;
import com.quotation.domain.enums.QuotationStatus;
import com.quotation.domain.exception.ProductNotFoundException;
import com.quotation.domain.exception.QuotationNotFoundException;
import com.quotation.domain.exception.UserNotFoundException;
import com.quotation.domain.service.QuotationStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuotationService implements QuotationUseCase {

    private final QuotationRepositoryPort quotationRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final QuotationCalculationService calculationService;
    private final PdfGenerationPort pdfGenerationPort;

    private final QuotationStateMachine stateMachine = new QuotationStateMachine();

    private static final String QUOTATION_NUMBER_PREFIX = "Q-";
    private static final int QUOTATION_SUFFIX_LENGTH = 8;

    @Override
    public QuotationResponse createQuotation(QuotationRequest request) {
        User user = userRepositoryPort.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        Quotation quotation = Quotation.builder()
                .quotationNumber(generateQuotationNumber())
                .user(user)
                .notes(request.getNotes())
                .build();

        List<QuotationLine> lines = new ArrayList<>();
        for (QuotationLineRequest lineReq : request.getLines()) {
            Product product = productRepositoryPort.findById(lineReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(lineReq.getProductId()));

            lines.add(QuotationLine.builder()
                    .quotation(quotation)
                    .product(product)
                    .quantity(lineReq.getQuantity())
                    .description(lineReq.getDescription())
                    .build());
        }

        CalculationResult result = calculationService.calculate(
                lines, user.isRegular(), LocalDate.now());

        quotation.setLines(result.getLines());
        quotation.setValidityDays(result.getValidityDays());
        quotation.setSubtotal(result.getSubtotal());
        quotation.setDiscountAmount(result.getDiscountAmount());
        quotation.setTotal(result.getTotal());

        return toResponse(quotationRepositoryPort.save(quotation));
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(Long id) {
        return toResponse(quotationRepositoryPort.findById(id)
                .orElseThrow(() -> new QuotationNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationResponse> getAllQuotations() {
        return quotationRepositoryPort.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public QuotationResponse updateStatus(Long id, QuotationStatus status) {
        Quotation quotation = quotationRepositoryPort.findById(id)
                .orElseThrow(() -> new QuotationNotFoundException(id));
        stateMachine.validateTransition(quotation.getStatus(), status);
        quotation.setStatus(status);
        return toResponse(quotationRepositoryPort.save(quotation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationResponse> getQuotationsByUserId(Long userId) {
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return quotationRepositoryPort.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getQuotationPdf(Long id) {
        QuotationResponse quotation = getQuotationById(id);
        return pdfGenerationPort.generateQuotationPdf(quotation);
    }

    private String generateQuotationNumber() {
        String suffix = UUID.randomUUID().toString().substring(0, QUOTATION_SUFFIX_LENGTH).toUpperCase();
        return QUOTATION_NUMBER_PREFIX + LocalDate.now().getYear() + "-" + suffix;
    }

    private QuotationResponse toResponse(Quotation quotation) {
        List<QuotationLineResponse> lineResponses = quotation.getLines().stream()
                .map(this::toLineResponse)
                .toList();

        return QuotationResponse.builder()
                .id(quotation.getId())
                .quotationNumber(quotation.getQuotationNumber())
                .userId(quotation.getUser().getId())
                .userName(quotation.getUser().getName())
                .userEmail(quotation.getUser().getEmail())
                .status(quotation.getStatus())
                .createdAt(quotation.getCreatedAt())
                .expiryDate(quotation.getExpiryDate())
                .validityDays(quotation.getValidityDays())
                .subtotal(quotation.getSubtotal())
                .discountAmount(quotation.getDiscountAmount())
                .total(quotation.getTotal())
                .notes(quotation.getNotes())
                .lines(lineResponses)
                .build();
    }

    private QuotationLineResponse toLineResponse(QuotationLine line) {
        return QuotationLineResponse.builder()
                .id(line.getId())
                .productId(line.getProduct().getId())
                .productName(line.getProduct().getName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .discountPercent(line.getDiscountPercent())
                .lineTotal(line.getLineTotal())
                .description(line.getDescription())
                .discountBreakdown(line.getDiscountBreakdown())
                .build();
    }
}
