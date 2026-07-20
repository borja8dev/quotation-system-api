package com.quotation.domain.service;

import com.quotation.domain.enums.QuotationStatus;
import com.quotation.domain.exception.InvalidStatusTransitionException;

import java.util.Map;
import java.util.Set;

public class QuotationStateMachine {

    private static final Map<QuotationStatus, Set<QuotationStatus>> VALID_TRANSITIONS = Map.of(
            QuotationStatus.DRAFT, Set.of(QuotationStatus.SENT, QuotationStatus.EXPIRED),
            QuotationStatus.SENT, Set.of(QuotationStatus.ACCEPTED, QuotationStatus.REJECTED, QuotationStatus.EXPIRED),
            QuotationStatus.ACCEPTED, Set.of(QuotationStatus.ARCHIVED, QuotationStatus.EXPIRED),
            QuotationStatus.REJECTED, Set.of(QuotationStatus.EXPIRED),
            QuotationStatus.EXPIRED, Set.of(),
            QuotationStatus.ARCHIVED, Set.of()
    );

    public void validateTransition(QuotationStatus from, QuotationStatus to) {
        Set<QuotationStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
