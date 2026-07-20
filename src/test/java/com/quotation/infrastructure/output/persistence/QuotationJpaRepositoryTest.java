package com.quotation.infrastructure.output.persistence;

import com.quotation.domain.entity.Product;
import com.quotation.domain.entity.Quotation;
import com.quotation.domain.entity.QuotationLine;
import com.quotation.domain.entity.User;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.SaleUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuotationJpaRepositoryTest {

    @Autowired
    private QuotationJpaRepository quotationJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private TestEntityManager em;

    private User savedUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedUser = userJpaRepository.save(User.builder()
                .name("Test User")
                .email("user@example.com")
                .phone("600000000")
                .build());

        savedProduct = productJpaRepository.save(Product.builder()
                .name("Tablero")
                .category(ProductCategory.TABLERO)
                .saleUnit(SaleUnit.UNIDAD)
                .pricePerUnit(BigDecimal.valueOf(50))
                .build());
    }

    @Test
    void save_WhenValidQuotation_ThenPersistedWithId() {
        Quotation quotation = buildQuotation("Q-001");

        Quotation saved = quotationJpaRepository.save(quotation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuotationNumber()).isEqualTo("Q-001");
    }

    @Test
    void findById_WhenExists_ThenReturnsQuotation() {
        Quotation saved = quotationJpaRepository.save(buildQuotation("Q-002"));

        Optional<Quotation> result = quotationJpaRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void findByUserId_WhenQuotationsExist_ThenReturnsAll() {
        quotationJpaRepository.save(buildQuotation("Q-003"));
        quotationJpaRepository.save(buildQuotation("Q-004"));

        List<Quotation> result = quotationJpaRepository.findByUserId(savedUser.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void findByUserId_WhenNoQuotations_ThenReturnsEmpty() {
        User otherUser = userJpaRepository.save(User.builder()
                .name("Other User")
                .email("other@example.com")
                .phone("611111111")
                .build());

        List<Quotation> result = quotationJpaRepository.findByUserId(otherUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void save_WithLines_WhenCascadeAll_ThenLinesPersisted() {
        Quotation quotation = buildQuotation("Q-005");
        QuotationLine line = buildLine(quotation);
        quotation.getLines().add(line);

        quotationJpaRepository.save(quotation);
        em.flush();
        em.clear();

        Quotation loaded = quotationJpaRepository.findById(quotation.getId()).orElseThrow();
        assertThat(loaded.getLines()).hasSize(1);
        assertThat(loaded.getLines().get(0).getProduct().getId()).isEqualTo(savedProduct.getId());
    }

    @Test
    void save_WhenLineRemovedFromCollection_ThenOrphanDeleted() {
        Quotation quotation = buildQuotation("Q-006");
        QuotationLine line1 = buildLine(quotation);
        QuotationLine line2 = buildLine(quotation);
        quotation.getLines().add(line1);
        quotation.getLines().add(line2);

        quotationJpaRepository.save(quotation);
        em.flush();
        em.clear();

        Quotation loaded = quotationJpaRepository.findById(quotation.getId()).orElseThrow();
        assertThat(loaded.getLines()).hasSize(2);

        loaded.getLines().remove(0);
        quotationJpaRepository.save(loaded);
        em.flush();
        em.clear();

        Quotation afterRemoval = quotationJpaRepository.findById(quotation.getId()).orElseThrow();
        assertThat(afterRemoval.getLines()).hasSize(1);
    }

    private Quotation buildQuotation(String number) {
        return Quotation.builder()
                .quotationNumber(number)
                .user(savedUser)
                .build();
    }

    private QuotationLine buildLine(Quotation quotation) {
        return QuotationLine.builder()
                .quotation(quotation)
                .product(savedProduct)
                .quantity(BigDecimal.valueOf(2))
                .unitPrice(BigDecimal.valueOf(50))
                .lineTotal(BigDecimal.valueOf(100))
                .build();
    }
}
