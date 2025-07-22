package com.openclassroom.paymybuddy.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ChangePasswordDTO.
 */
class ChangePasswordDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidChangePasswordDTO() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("currentPass123", "newPass123", "newPass123");

        // When
        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
        assertTrue(dto.isPasswordMatching());
    }

    @Test
    void testCurrentPasswordBlank() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("", "newPass123", "newPass123");

        // When
        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("mot de passe actuel")));
    }

    @Test
    void testNewPasswordTooShort() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("currentPass123", "short1", "short1");

        // When
        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("entre 8 et 100")));
    }

    @Test
    void testNewPasswordNoLetter() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("currentPass123", "12345678", "12345678");

        // When
        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("lettre et un chiffre")));
    }

    @Test
    void testNewPasswordNoDigit() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("currentPass123", "abcdefgh", "abcdefgh");

        // When
        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("lettre et un chiffre")));
    }

    @Test
    void testPasswordsNotMatching() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("currentPass123", "newPass123", "differentPass123");

        // When
        boolean matching = dto.isPasswordMatching();

        // Then
        assertFalse(matching);
    }

    @Test
    void testPasswordsMatchingWithNulls() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("currentPass123", null, null);

        // When
        boolean matching = dto.isPasswordMatching();

        // Then
        assertFalse(matching);
    }

    @Test
    void testToString() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO("currentPass123", "newPass123", "newPass123");

        // When
        String toString = dto.toString();

        // Then
        assertNotNull(toString);
        assertFalse(toString.contains("currentPass123"));
        assertFalse(toString.contains("newPass123"));
        assertTrue(toString.contains("[PROTECTED]"));
    }
}
