package com.openclassroom.paymybuddy.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ValidationUtils.
 */
class ValidationUtilsTest {

    private ValidationUtils validationUtils;

    @BeforeEach
    void setUp() {
        validationUtils = new ValidationUtils();
    }

    // Tests pour isValidPassword
    @Test
    void testValidPassword() {
        System.out.println("Testing password123 (length: " + "password123".length() + ")");
        boolean result1 = validationUtils.isValidPassword("password123");
        System.out.println("Result: " + result1);
        
        System.out.println("Testing MyPass1 (length: " + "MyPass1".length() + ")");
        boolean result2 = validationUtils.isValidPassword("MyPass10");
        System.out.println("Result: " + result2);
        
        assertTrue(result1, "password123 should be valid");
        assertTrue(result2, "MyPass10 should be valid");
        assertTrue(validationUtils.isValidPassword("ComplexP@ssw0rd"));
    }

    @Test
    void testInvalidPasswordNull() {
        assertFalse(validationUtils.isValidPassword(null));
    }

    @Test
    void testInvalidPasswordEmpty() {
        assertFalse(validationUtils.isValidPassword(""));
        assertFalse(validationUtils.isValidPassword("   "));
    }

    @Test
    void testInvalidPasswordTooShort() {
        assertFalse(validationUtils.isValidPassword("pass1"));
        assertFalse(validationUtils.isValidPassword("1234567"));
    }

    @Test
    void testInvalidPasswordNoLetter() {
        assertFalse(validationUtils.isValidPassword("12345678"));
    }

    @Test
    void testInvalidPasswordNoDigit() {
        assertFalse(validationUtils.isValidPassword("password"));
    }

    // Tests pour isValidUsername
    @Test
    void testValidUsername() {
        assertTrue(validationUtils.isValidUsername("user123"));
        assertTrue(validationUtils.isValidUsername("User"));
        assertTrue(validationUtils.isValidUsername("username"));
        assertTrue(validationUtils.isValidUsername("123456"));
    }

    @Test
    void testInvalidUsernameNull() {
        assertFalse(validationUtils.isValidUsername(null));
    }

    @Test
    void testInvalidUsernameEmpty() {
        assertFalse(validationUtils.isValidUsername(""));
        assertFalse(validationUtils.isValidUsername("   "));
    }

    @Test
    void testInvalidUsernameTooShort() {
        assertFalse(validationUtils.isValidUsername("ab"));
        assertFalse(validationUtils.isValidUsername("1"));
    }

    @Test
    void testInvalidUsernameTooLong() {
        assertFalse(validationUtils.isValidUsername("a".repeat(21)));
    }

    @Test
    void testInvalidUsernameSpecialCharacters() {
        assertFalse(validationUtils.isValidUsername("user@123"));
        assertFalse(validationUtils.isValidUsername("user.name"));
        assertFalse(validationUtils.isValidUsername("user-name"));
        assertFalse(validationUtils.isValidUsername("user name"));
    }

    // Tests pour isValidEmail
    @Test
    void testValidEmail() {
        assertTrue(validationUtils.isValidEmail("user@example.com"));
        assertTrue(validationUtils.isValidEmail("test.email@domain.org"));
        assertTrue(validationUtils.isValidEmail("user123@example123.com"));
        assertTrue(validationUtils.isValidEmail("user+tag@example.com"));
    }

    @Test
    void testInvalidEmailNull() {
        assertFalse(validationUtils.isValidEmail(null));
    }

    @Test
    void testInvalidEmailEmpty() {
        assertFalse(validationUtils.isValidEmail(""));
        assertFalse(validationUtils.isValidEmail("   "));
    }

    @Test
    void testInvalidEmailFormat() {
        assertFalse(validationUtils.isValidEmail("invalid-email"));
        assertFalse(validationUtils.isValidEmail("user@"));
        assertFalse(validationUtils.isValidEmail("@example.com"));
        assertFalse(validationUtils.isValidEmail("userexample.com"));
        assertFalse(validationUtils.isValidEmail("user@@example.com"));
    }

    // Tests pour areEqual
    @Test
    void testAreEqualBothNull() {
        assertTrue(validationUtils.areEqual(null, null));
    }

    @Test
    void testAreEqualOneNull() {
        assertFalse(validationUtils.areEqual("test", null));
        assertFalse(validationUtils.areEqual(null, "test"));
    }

    @Test
    void testAreEqualSameStrings() {
        assertTrue(validationUtils.areEqual("test", "test"));
        assertTrue(validationUtils.areEqual("", ""));
    }

    @Test
    void testAreEqualDifferentStrings() {
        assertFalse(validationUtils.areEqual("test1", "test2"));
        assertFalse(validationUtils.areEqual("Test", "test"));
    }
}
