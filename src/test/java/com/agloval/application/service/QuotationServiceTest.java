package com.agloval.application.service;

import com.agloval.application.dto.QuotationLineRequest;
import com.agloval.application.dto.QuotationRequest;
import com.agloval.application.dto.QuotationResponse;
import com.agloval.application.port.out.PdfGenerationPort;
import com.agloval.application.port.out.ProductRepositoryPort;
import com.agloval.application.port.out.QuotationRepositoryPort;
import com.agloval.application.port.out.UserRepositoryPort;
import com.agloval.domain.entity.Product;
import com.agloval.domain.entity.Quotation;
import com.agloval.domain.entity.User;
import com.agloval.domain.enums.ProductCategory;
import com.agloval.domain.enums.QuotationStatus;
import com.agloval.domain.enums.SaleUnit;
import com.agloval.domain.exception.InvalidStatusTransitionException;
import com.agloval.domain.exception.QuotationNotFoundException;
import com.agloval.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private QuotationRepositoryPort quotationRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private PdfGenerationPort pdfGenerationPort;

    private QuotationService quotationService;

    private User testUser;
    private Product boardProduct;

    @BeforeEach
    void setUp() {
        QuotationCalculationService calculationService = new QuotationCalculationService();
        quotationService = new QuotationService(
                quotationRepositoryPort, userRepositoryPort, productRepositoryPort,
                calculationService, pdfGenerationPort);

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .phone("123456789")
                .regular(false)
                .build();

        boardProduct = Product.builder()
                .id(1L)
                .name("Oak Board 18mm")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .pricePerUnit(new BigDecimal("45.00"))
                .widthCm(244)
                .lengthCm(122)
                .thicknessMm(18)
                .build();
    }

    @Test
    void createQuotation_WhenValidRequest_ThenReturnsCalculatedQuotation() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(boardProduct));
        when(quotationRepositoryPort.save(any(Quotation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuotationRequest request = QuotationRequest.builder()
                .userId(1L)
                .lines(List.of(QuotationLineRequest.builder()
                        .productId(1L)
                        .quantity(new BigDecimal("10"))
                        .build()))
                .build();

        QuotationResponse response = quotationService.createQuotation(request);

        assertThat(response.getSubtotal()).isEqualByComparingTo("450.00");
        assertThat(response.getTotal()).isEqualByComparingTo("450.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("0.00");
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("45.00");
    }

    @Test
    void createQuotation_WhenVolumeDiscount_ThenAppliesThreePercent() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(boardProduct));
        when(quotationRepositoryPort.save(any(Quotation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuotationRequest request = QuotationRequest.builder()
                .userId(1L)
                .lines(List.of(QuotationLineRequest.builder()
                        .productId(1L)
                        .quantity(new BigDecimal("24"))
                        .build()))
                .build();

        QuotationResponse response = quotationService.createQuotation(request);

        // 24 boards * 45€ = 1080€ gross, 3% discount → 1080 * 0.97 = 1047.60 net
        assertThat(response.getTotal()).isEqualByComparingTo("1047.60");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("32.40");
        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo("3");
    }

    @Test
    void createQuotation_WhenUserNotFound_ThenThrows() {
        when(userRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

        QuotationRequest request = QuotationRequest.builder()
                .userId(99L)
                .lines(List.of(QuotationLineRequest.builder()
                        .productId(1L)
                        .quantity(new BigDecimal("1"))
                        .build()))
                .build();

        assertThatThrownBy(() -> quotationService.createQuotation(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createQuotation_WhenRegularCustomer_ThenAppliesExtraDiscount() {
        User regularUser = User.builder()
                .id(2L).name("Regular").email("regular@test.com").phone("000").regular(true).build();

        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(regularUser));
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(boardProduct));
        when(quotationRepositoryPort.save(any(Quotation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuotationRequest request = QuotationRequest.builder()
                .userId(2L)
                .lines(List.of(QuotationLineRequest.builder()
                        .productId(1L)
                        .quantity(new BigDecimal("10"))
                        .build()))
                .build();

        QuotationResponse response = quotationService.createQuotation(request);

        // 10 boards (no volume discount) + 2% regular = 2%
        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo("2");
        assertThat(response.getLines().get(0).getDiscountBreakdown()).contains("Regular customer");
    }

    @Test
    void updateStatus_WhenValidTransition_ThenUpdatesStatus() {
        Quotation quotation = Quotation.builder()
                .id(1L).quotationNumber("Q-2026-TEST").user(testUser).status(QuotationStatus.DRAFT).build();

        when(quotationRepositoryPort.findById(1L)).thenReturn(Optional.of(quotation));
        when(quotationRepositoryPort.save(any(Quotation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuotationResponse response = quotationService.updateStatus(1L, QuotationStatus.SENT);

        assertThat(response.getStatus()).isEqualTo(QuotationStatus.SENT);
    }

    @Test
    void updateStatus_WhenInvalidTransition_ThenThrows() {
        Quotation quotation = Quotation.builder()
                .id(1L).quotationNumber("Q-2026-TEST").user(testUser).status(QuotationStatus.DRAFT).build();

        when(quotationRepositoryPort.findById(1L)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.updateStatus(1L, QuotationStatus.ACCEPTED))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void updateStatus_WhenQuotationNotFound_ThenThrows() {
        when(quotationRepositoryPort.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quotationService.updateStatus(99L, QuotationStatus.SENT))
                .isInstanceOf(QuotationNotFoundException.class);
    }
}
