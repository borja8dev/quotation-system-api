package com.quotation.domain.service;

import com.quotation.domain.enums.QuotationStatus;
import com.quotation.domain.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuotationStateMachineTest {

    private final QuotationStateMachine stateMachine = new QuotationStateMachine();

    @Test
    void validateTransition_WhenDraftToSent_ThenNoException() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.DRAFT, QuotationStatus.SENT));
    }

    @Test
    void validateTransition_WhenSentToAccepted_ThenNoException() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.SENT, QuotationStatus.ACCEPTED));
    }

    @Test
    void validateTransition_WhenSentToRejected_ThenNoException() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.SENT, QuotationStatus.REJECTED));
    }

    @Test
    void validateTransition_WhenAcceptedToArchived_ThenNoException() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.ACCEPTED, QuotationStatus.ARCHIVED));
    }

    @Test
    void validateTransition_WhenDraftToAccepted_ThenThrows() {
        assertThatThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.DRAFT, QuotationStatus.ACCEPTED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("ACCEPTED");
    }

    @Test
    void validateTransition_WhenDraftToRejected_ThenThrows() {
        assertThatThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.DRAFT, QuotationStatus.REJECTED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("REJECTED");
    }

    @Test
    void validateTransition_WhenRejectedToAccepted_ThenThrows() {
        assertThatThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.REJECTED, QuotationStatus.ACCEPTED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("REJECTED")
                .hasMessageContaining("ACCEPTED");
    }

    @Test
    void validateTransition_WhenArchivedToAny_ThenThrows() {
        for (QuotationStatus target : QuotationStatus.values()) {
            if (target != QuotationStatus.ARCHIVED) {
                assertThatThrownBy(() ->
                        stateMachine.validateTransition(QuotationStatus.ARCHIVED, target))
                        .isInstanceOf(InvalidStatusTransitionException.class);
            }
        }
    }

    @Test
    void validateTransition_WhenAnyToExpired_ThenNoException() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.DRAFT, QuotationStatus.EXPIRED));
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.SENT, QuotationStatus.EXPIRED));
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.ACCEPTED, QuotationStatus.EXPIRED));
        assertThatNoException().isThrownBy(() ->
                stateMachine.validateTransition(QuotationStatus.REJECTED, QuotationStatus.EXPIRED));
    }
}
