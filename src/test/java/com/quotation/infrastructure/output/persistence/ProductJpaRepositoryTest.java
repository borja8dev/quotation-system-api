package com.quotation.infrastructure.output.persistence;

import com.quotation.domain.entity.Product;
import com.quotation.domain.enums.ProductCategory;
import com.quotation.domain.enums.SaleUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductJpaRepositoryTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    void save_WhenValidProduct_ThenPersistedWithId() {
        Product product = buildProduct("Tablero MDF 18mm", ProductCategory.TABLERO);

        Product saved = productJpaRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Tablero MDF 18mm");
    }

    @Test
    void findById_WhenExists_ThenReturnsProduct() {
        Product saved = productJpaRepository.save(buildProduct("Perfil Aluminio", ProductCategory.PERFILERIA_ALUMINIO));

        Optional<Product> result = productJpaRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCategory()).isEqualTo(ProductCategory.PERFILERIA_ALUMINIO);
    }

    @Test
    void findAll_WhenMultipleProducts_ThenReturnsAll() {
        productJpaRepository.save(buildProduct("Product A", ProductCategory.FERRETERIA));
        productJpaRepository.save(buildProduct("Product B", ProductCategory.SERVICIO));

        List<Product> products = productJpaRepository.findAll();

        assertThat(products).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void existsById_WhenExists_ThenTrue() {
        Product saved = productJpaRepository.save(buildProduct("Puerta Cocina", ProductCategory.PUERTA_COCINA));

        assertThat(productJpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void deleteById_WhenExists_ThenProductRemoved() {
        Product saved = productJpaRepository.save(buildProduct("Módulo Cocina", ProductCategory.MODULO_COCINA));

        productJpaRepository.deleteById(saved.getId());

        assertThat(productJpaRepository.findById(saved.getId())).isEmpty();
    }

    private Product buildProduct(String name, ProductCategory category) {
        return Product.builder()
                .name(name)
                .category(category)
                .saleUnit(SaleUnit.UNIDAD)
                .pricePerUnit(BigDecimal.valueOf(99.99))
                .build();
    }
}
