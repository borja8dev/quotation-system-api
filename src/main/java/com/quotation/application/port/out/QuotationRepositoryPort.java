package com.quotation.application.port.out;

import com.quotation.domain.entity.Quotation;

import java.util.List;
import java.util.Optional;

public interface QuotationRepositoryPort {

    Quotation save(Quotation quotation);

    Optional<Quotation> findById(Long id);

    List<Quotation> findAll();

    List<Quotation> findByUserId(Long userId);
}
