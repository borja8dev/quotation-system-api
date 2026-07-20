package com.quotation.infrastructure.output.persistence;

import com.quotation.application.port.out.QuotationRepositoryPort;
import com.quotation.domain.entity.Quotation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuotationRepositoryAdapter implements QuotationRepositoryPort {

    private final QuotationJpaRepository quotationJpaRepository;

    @Override
    public Quotation save(Quotation quotation) {
        return quotationJpaRepository.save(quotation);
    }

    @Override
    public Optional<Quotation> findById(Long id) {
        return quotationJpaRepository.findById(id);
    }

    @Override
    public List<Quotation> findAll() {
        return quotationJpaRepository.findAll();
    }

    @Override
    public List<Quotation> findByUserId(Long userId) {
        return quotationJpaRepository.findByUserId(userId);
    }
}
