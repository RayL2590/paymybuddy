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
 * Tests unitaires pour ChangeUsernameDTO.
 */
class ChangeUsernameDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidChangeUsernameDTO() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("newuser123");

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUsernameBlank() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("");

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nom d'utilisateur est requis")));
    }

    @Test
    void testUsernameTooShort() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("ab");

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("entre 3 et 20")));
    }

    @Test
    void testUsernameTooLong() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("a".repeat(21));

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("entre 3 et 20")));
    }

    @Test
    void testUsernameWithSpecialCharacters() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("user@123");

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("lettres et des chiffres")));
    }

    @Test
    void testUsernameWithSpaces() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("user 123");

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("lettres et des chiffres")));
    }

    @Test
    void testValidUsernameAlphabetic() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("username");

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidUsernameNumeric() {
        // Given
        ChangeUsernameDTO dto = new ChangeUsernameDTO("123456");

        // When
        Set<ConstraintViolation<ChangeUsernameDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }
}
