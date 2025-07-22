package com.openclassroom.paymybuddy.service;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@mail.com")
                .password("motdepasse1")
                .balance(BigDecimal.ZERO)
                .role("USER")
                .build();
    }

    @Test
    void getCurrentUser_WithAuthenticatedUser_ShouldReturnUser() {
        // Given
        String userEmail = "alice@mail.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        // When
        User result = authService.getCurrentUser();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(userEmail);
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void getCurrentUser_WithNonAuthenticatedUser_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // When
        User result = authService.getCurrentUser();

        // Then
        assertThat(result).isNull();
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getCurrentUser_WithNullAuthentication_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        User result = authService.getCurrentUser();

        // Then
        assertThat(result).isNull();
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getCurrentUser_WithUserNotFoundInDatabase_ShouldReturnNull() {
        // Given
        String userEmail = "nonexistent@example.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);

        // When
        User result = authService.getCurrentUser();

        // Then
        assertThat(result).isNull();
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void getCurrentUserId_WithAuthenticatedUser_ShouldReturnUserId() {
        // Given
        String userEmail = "test@example.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        // When
        Long result = authService.getCurrentUserId();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser.getId());
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void getCurrentUserId_WithNonAuthenticatedUser_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // When
        Long result = authService.getCurrentUserId();

        // Then
        assertThat(result).isNull();
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void isAuthenticated_WithAuthenticatedUser_ShouldReturnTrue() {
        // Given
        String userEmail = "test@example.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("userPrincipal"); 
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = authService.isAuthenticated();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isAuthenticated_WithNonAuthenticatedUser_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = authService.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isAuthenticated_WithNullAuthentication_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = authService.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isAuthenticated_WithAnonymousUser_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.setContext(securityContext);

        // When
        boolean result = authService.isAuthenticated();

        // Then
        assertThat(result).isFalse();
        verify(userRepository, never()).findByEmail(anyString());
    }
}
