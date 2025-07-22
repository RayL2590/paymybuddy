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
 * Tests unitaires pour ChangeEmailDTO.
 */
class ChangeEmailDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidChangeEmailDTO() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("newemail@example.com");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmailBlank() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("email est requis")));
    }

    @Test
    void testEmailNull() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO(null);

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("email est requis")));
    }

    @Test
    void testInvalidEmailFormat() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("invalid-email");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("format valide")));
    }

    @Test
    void testEmailWithoutDomain() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("user@");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("format valide")));
    }

    @Test
    void testEmailWithoutAtSymbol() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("userexample.com");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("format valide")));
    }

    @Test
    void testValidEmailWithSubdomain() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("user@mail.example.com");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidEmailWithNumbers() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("user123@example123.com");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidEmailWithSpecialCharacters() {
        // Given
        ChangeEmailDTO dto = new ChangeEmailDTO("user.name+tag@example.com");

        // When
        Set<ConstraintViolation<ChangeEmailDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }
}
