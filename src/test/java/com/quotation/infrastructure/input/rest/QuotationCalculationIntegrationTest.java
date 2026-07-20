package com.quotation.infrastructure.input.rest;

import com.quotation.application.dto.QuotationLineRequest;
import com.quotation.application.dto.QuotationRequest;
import com.quotation.application.dto.QuotationResponse;
import com.quotation.application.port.in.QuotationUseCase;
import com.quotation.application.port.out.ProductRepositoryPort;
import com.quotation.application.port.out.UserRepositoryPort;
import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.User;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.QuotationStatus;
import com.quotation.domain.enums.RateType;
import com.quotation.domain.enums.SaleUnit;
import com.quotation.domain.exception.InvalidStatusTransitionException;
import com.quotation.domain.exception.QuotationValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuotationCalculationIntegrationTest {

    @Autowired
    private QuotationUseCase quotationUseCase;

    @Autowired
    private ProductRepositoryPort productRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    // Users
    private Long nonRegularUserId;
    private Long regularUserId;

    // Products
    private Long boardProduct18mmId;      // 244x122, 18mm, 45.00/unit
    private Long boardProduct16mmId;      // 244x122, 16mm, 40.00/unit
    private Long serviceProductId;        // SERVICIO, PER_MINUTE, 1.80/rate_unit
    private Long hardwareProductId;       // FERRETERIA, 5.50/unit
    private Long invalidThicknessBoardId; // 244x122, 2mm — violates 4-40mm rule
    private Long nonStandardDimsProductId;// 300x100 — non-standard dimensions

    @BeforeEach
    void setUp() {
        User nonRegularUser = userRepositoryPort.save(User.builder()
                .name("Regular User")
                .email("client.standard@quotation.test")
                .phone("600000001")
                .regular(false)
                .password("$2a$12$hashedpassword")
                .build());
        nonRegularUserId = nonRegularUser.getId();

        User regularUser = userRepositoryPort.save(User.builder()
                .name("Regular VIP User")
                .email("client.regular@quotation.test")
                .phone("600000002")
                .regular(true)
                .password("$2a$12$hashedpassword")
                .build());
        regularUserId = regularUser.getId();

        Product board18mm = productRepositoryPort.save(Product.builder()
                .name("Oak Board 18mm 244x122")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .widthCm(244)
                .lengthCm(122)
                .thicknessMm(18)
                .pricePerUnit(new BigDecimal("45.00"))
                .build());
        boardProduct18mmId = board18mm.getId();

        Product board16mm = productRepositoryPort.save(Product.builder()
                .name("Oak Board 16mm 244x122")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .widthCm(244)
                .lengthCm(122)
                .thicknessMm(16)
                .pricePerUnit(new BigDecimal("40.00"))
                .build());
        boardProduct16mmId = board16mm.getId();

        Product service = productRepositoryPort.save(Product.builder()
                .name("CNC Cutting Service")
                .category(ProductCategory.SERVICIO)
                .saleUnit(SaleUnit.MINUTO)
                .rateType(RateType.PER_MINUTE)
                .pricePerRateUnit(new BigDecimal("1.80"))
                .build());
        serviceProductId = service.getId();

        Product hardware = productRepositoryPort.save(Product.builder()
                .name("Hinge")
                .category(ProductCategory.FERRETERIA)
                .saleUnit(SaleUnit.UNIDAD)
                .pricePerUnit(new BigDecimal("5.50"))
                .build());
        hardwareProductId = hardware.getId();

        Product invalidThicknessBoard = productRepositoryPort.save(Product.builder()
                .name("Ultra-Thin Board 2mm")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .widthCm(244)
                .lengthCm(122)
                .thicknessMm(2)
                .pricePerUnit(new BigDecimal("30.00"))
                .build());
        invalidThicknessBoardId = invalidThicknessBoard.getId();

        Product nonStandardDimsProduct = productRepositoryPort.save(Product.builder()
                .name("Non-Standard Board")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.TABLERO)
                .widthCm(300)
                .lengthCm(100)
                .thicknessMm(18)
                .pricePerUnit(new BigDecimal("35.00"))
                .build());
        nonStandardDimsProductId = nonStandardDimsProduct.getId();
    }

    // -------------------------------------------------------------------------
    // Calculation tests
    // -------------------------------------------------------------------------

    @Test
    void createQuotation_WhenSingleBoard_ThenNoVolumeDiscount() {
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct18mmId)
                                .quantity(new BigDecimal("10"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        // 10 * 45.00 = 450.00, no discount
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(response.getStatus()).isEqualTo(QuotationStatus.DRAFT);
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createQuotation_WhenExactly24Boards_ThenThreePercentDiscount() {
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct18mmId)
                                .quantity(new BigDecimal("24"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        // 24 * 45.00 = 1080.00; 3% discount → discountAmt = 32.40; total = 1047.60
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1080.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("32.40"));
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("1047.60"));
        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    void createQuotation_WhenExactly48Boards_ThenSixPercentDiscount() {
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct18mmId)
                                .quantity(new BigDecimal("48"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        // 48 * 45.00 = 2160.00; 6% discount → discountAmt = 129.60; total = 2030.40
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("2160.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("129.60"));
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("2030.40"));
        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo(new BigDecimal("6"));
    }

    @Test
    void createQuotation_When16mmBoards48Plus_ThenStacksVolumeAndThickness() {
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct16mmId)
                                .quantity(new BigDecimal("48"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        // 48 * 40.00 = 1920.00; 6% volume + 3% thickness = 9% → discountAmt = 172.80; total = 1747.20
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1920.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("172.80"));
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("1747.20"));
        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo(new BigDecimal("9"));
    }

    @Test
    void createQuotation_WhenRegularCustomer48Plus16mm_ThenStacksAllDiscounts() {
        QuotationRequest request = QuotationRequest.builder()
                .userId(regularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct16mmId)
                                .quantity(new BigDecimal("48"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        // 48 * 40.00 = 1920.00; 6% + 3% + 2% = 11% → discountAmt = 211.20; total = 1708.80
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1920.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("211.20"));
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("1708.80"));
        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo(new BigDecimal("11"));
        assertThat(response.getLines().get(0).getDiscountBreakdown()).contains("Volume 6%");
        assertThat(response.getLines().get(0).getDiscountBreakdown()).contains("16mm bonus 3%");
        assertThat(response.getLines().get(0).getDiscountBreakdown()).contains("Regular customer 2%");
    }

    @Test
    void createQuotation_WhenMixedProducts_ThenBoardDiscountsOnlyOnBoards() {
        // 24 boards (18mm, 45€) trigger 3% volume discount; 10 service minutes (1.80€) get no discount
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct18mmId)
                                .quantity(new BigDecimal("24"))
                                .build(),
                        QuotationLineRequest.builder()
                                .productId(serviceProductId)
                                .quantity(new BigDecimal("10"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        // Board: 24 * 45.00 = 1080.00, 3% → lineTotal = 1047.60, discAmt = 32.40
        // Service: 10 * 1.80 = 18.00, 0% → lineTotal = 18.00, discAmt = 0
        // subtotal = 1098.00, totalDiscount = 32.40, total = 1065.60
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1098.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("32.40"));
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("1065.60"));

        assertThat(response.getLines().get(0).getDiscountPercent()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(response.getLines().get(1).getDiscountPercent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getLines().get(1).getDiscountBreakdown()).isNull();
    }

    @Test
    void createQuotation_WhenServiceProduct_ThenUsesRatePrice() {
        // 30 minutes of CNC at 1.80/min → total = 54.00
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(serviceProductId)
                                .quantity(new BigDecimal("30"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("54.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("54.00"));
        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("1.80"));
    }

    // -------------------------------------------------------------------------
    // Validation tests
    // -------------------------------------------------------------------------

    @Test
    void createQuotation_WhenInvalidThickness_ThenThrowsValidation() {
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(invalidThicknessBoardId)
                                .quantity(new BigDecimal("5"))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> quotationUseCase.createQuotation(request))
                .isInstanceOf(QuotationValidationException.class)
                .hasMessageContaining("thickness");
    }

    @Test
    void createQuotation_WhenNonStandardDimensions_ThenThrowsValidation() {
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(nonStandardDimsProductId)
                                .quantity(new BigDecimal("5"))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> quotationUseCase.createQuotation(request))
                .isInstanceOf(QuotationValidationException.class)
                .hasMessageContaining("dimensions");
    }

    // -------------------------------------------------------------------------
    // State machine tests
    // -------------------------------------------------------------------------

    @Test
    void updateStatus_WhenDraftToSent_ThenSucceeds() {
        QuotationResponse created = quotationUseCase.createQuotation(minimalRequest(nonRegularUserId));

        QuotationResponse updated = quotationUseCase.updateStatus(created.getId(), QuotationStatus.SENT);

        assertThat(updated.getStatus()).isEqualTo(QuotationStatus.SENT);
    }

    @Test
    void updateStatus_WhenSentToAccepted_ThenSucceeds() {
        QuotationResponse created = quotationUseCase.createQuotation(minimalRequest(nonRegularUserId));
        quotationUseCase.updateStatus(created.getId(), QuotationStatus.SENT);

        QuotationResponse updated = quotationUseCase.updateStatus(created.getId(), QuotationStatus.ACCEPTED);

        assertThat(updated.getStatus()).isEqualTo(QuotationStatus.ACCEPTED);
    }

    @Test
    void updateStatus_WhenDraftToAccepted_ThenThrowsInvalidTransition() {
        QuotationResponse created = quotationUseCase.createQuotation(minimalRequest(nonRegularUserId));

        assertThatThrownBy(() -> quotationUseCase.updateStatus(created.getId(), QuotationStatus.ACCEPTED))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // -------------------------------------------------------------------------
    // Validity tests
    // -------------------------------------------------------------------------

    @Test
    void createQuotation_WhenNonRegularCustomer_ThenDefaultValidity() {
        QuotationResponse response = quotationUseCase.createQuotation(minimalRequest(nonRegularUserId));

        // Valid values: 30 (summer/holiday) or 45 (normal season)
        int month = LocalDate.now().getMonthValue();
        boolean isSummerOrHoliday = (month >= 6 && month <= 8) || month == 12;
        int expectedDays = isSummerOrHoliday ? 30 : 45;

        assertThat(response.getValidityDays()).isEqualTo(expectedDays);
    }

    @Test
    void createQuotation_WhenRegularCustomer_ThenLongerValidity() {
        QuotationResponse nonRegularResponse = quotationUseCase.createQuotation(minimalRequest(nonRegularUserId));
        QuotationResponse regularResponse = quotationUseCase.createQuotation(minimalRequest(regularUserId));

        assertThat(regularResponse.getValidityDays()).isGreaterThan(nonRegularResponse.getValidityDays());
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void createQuotation_WhenMultipleBoardLines_ThenAggregatesForVolumeDiscount() {
        // Two lines of 15 boards each = 30 total → both qualify for 3% volume discount
        QuotationRequest request = QuotationRequest.builder()
                .userId(nonRegularUserId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct18mmId)
                                .quantity(new BigDecimal("15"))
                                .build(),
                        QuotationLineRequest.builder()
                                .productId(boardProduct18mmId)
                                .quantity(new BigDecimal("15"))
                                .build()
                ))
                .build();

        QuotationResponse response = quotationUseCase.createQuotation(request);

        // Each line: 15 * 45.00 = 675.00, 3% → lineTotal = 654.75, discAmt = 20.25
        // subtotal = 1350.00, totalDiscount = 40.50, total = 1309.50
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1350.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("40.50"));
        assertThat(response.getTotal()).isEqualByComparingTo(new BigDecimal("1309.50"));

        response.getLines().forEach(line ->
                assertThat(line.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("3")));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private QuotationRequest minimalRequest(Long userId) {
        return QuotationRequest.builder()
                .userId(userId)
                .lines(List.of(
                        QuotationLineRequest.builder()
                                .productId(boardProduct18mmId)
                                .quantity(new BigDecimal("1"))
                                .build()
                ))
                .build();
    }
}
