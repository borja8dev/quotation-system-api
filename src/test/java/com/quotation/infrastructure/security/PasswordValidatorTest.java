package com.quotation.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    void isValid_WhenAllRulesMet_ThenReturnsTrue() {
        assertTrue(validator.isValid("Test1234!"));
    }

    @Test
    void isValid_WhenTooShort_ThenReturnsFalse() {
        assertFalse(validator.isValid("Te1!"));
    }

    @Test
    void isValid_WhenNoUppercase_ThenReturnsFalse() {
        assertFalse(validator.isValid("test1234!"));
    }

    @Test
    void isValid_WhenNoDigit_ThenReturnsFalse() {
        assertFalse(validator.isValid("TestTest!"));
    }

    @Test
    void isValid_WhenNoSpecialChar_ThenReturnsFalse() {
        assertFalse(validator.isValid("Test12345"));
    }

    @Test
    void isValid_WhenNull_ThenReturnsFalse() {
        assertFalse(validator.isValid(null));
    }
}
