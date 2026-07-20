package com.quotation.application.service;

import com.quotation.application.dto.ProductRequest;
import com.quotation.application.dto.ProductResponse;
import com.quotation.application.port.in.ProductUseCase;
import com.quotation.application.port.out.ProductRepositoryPort;
import com.quotation.domain.entity.Product;
import com.quotation.domain.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        return toResponse(productRepositoryPort.save(toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toResponse(productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepositoryPort.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existing = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        existing.setName(request.getName());
        existing.setCategory(request.getCategory());
        existing.setSaleUnit(request.getSaleUnit());
        existing.setPricePerM2(request.getPricePerM2());
        existing.setPricePerUnit(request.getPricePerUnit());
        existing.setPricePerRateUnit(request.getPricePerRateUnit());
        existing.setRateType(request.getRateType());
        existing.setWidthCm(request.getWidthCm());
        existing.setLengthCm(request.getLengthCm());
        existing.setThicknessMm(request.getThicknessMm());
        existing.setColor(request.getColor());

        return toResponse(productRepositoryPort.save(existing));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepositoryPort.deleteById(id);
    }

    private Product toEntity(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .saleUnit(request.getSaleUnit())
                .pricePerM2(request.getPricePerM2())
                .pricePerUnit(request.getPricePerUnit())
                .pricePerRateUnit(request.getPricePerRateUnit())
                .rateType(request.getRateType())
                .widthCm(request.getWidthCm())
                .lengthCm(request.getLengthCm())
                .thicknessMm(request.getThicknessMm())
                .color(request.getColor())
                .build();
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .saleUnit(product.getSaleUnit())
                .pricePerM2(product.getPricePerM2())
                .pricePerUnit(product.getPricePerUnit())
                .pricePerRateUnit(product.getPricePerRateUnit())
                .rateType(product.getRateType())
                .widthCm(product.getWidthCm())
                .lengthCm(product.getLengthCm())
                .thicknessMm(product.getThicknessMm())
                .color(product.getColor())
                .build();
    }
}
