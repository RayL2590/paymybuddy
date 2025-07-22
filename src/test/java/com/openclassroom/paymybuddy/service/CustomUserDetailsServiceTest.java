package com.openclassroom.paymybuddy.service;

import com.openclassroom.paymybuddy.configuration.CustomUserDetailsService;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CustomUserDetailsService.
 * Teste l'authentification et la gestion des rôles utilisateur.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .balance(new BigDecimal("100.00"))
                .role("USER")
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("encodedAdminPassword")
                .balance(new BigDecimal("500.00"))
                .role("ADMIN")
                .build();
    }

    // ========== TESTS DE SUCCÈS ==========

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnCorrectAuthorities() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithAdminUser_ShouldReturnAdminAuthorities() {
        // Given
        String email = "admin@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedAdminPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(userRepository).findByEmail(email);
    }

    // ========== TESTS D'ÉCHEC ==========

    @Test
    void loadUserByUsername_WithNonExistingEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilisateur non trouvé: " + email);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithNullEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        String email = null;
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilisateur non trouvé: null");

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithEmptyEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        String email = "";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilisateur non trouvé: ");

        verify(userRepository).findByEmail(email);
    }

    // ========== TESTS DE RÔLES SPÉCIAUX ==========

    @Test
    void loadUserByUsername_WithCustomRole_ShouldReturnCorrectAuthorities() {
        // Given
        String email = "custom@example.com";
        User customUser = User.builder()
                .id(3L)
                .username("customuser")
                .email(email)
                .password("password")
                .role("MODERATOR")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(customUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MODERATOR");

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithNullRole_ShouldReturnRoleNull() {
        // Given
        String email = "nullrole@example.com";
        User userWithNullRole = User.builder()
                .id(4L)
                .username("nullroleuser")
                .email(email)
                .password("password")
                .role(null)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithNullRole));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_null");

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithEmptyRole_ShouldReturnRoleEmpty() {
        // Given
        String email = "emptyrole@example.com";
        User userWithEmptyRole = User.builder()
                .id(5L)
                .username("emptyroleuser")
                .email(email)
                .password("password")
                .role("")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithEmptyRole));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_");

        verify(userRepository).findByEmail(email);
    }

    // ========== TESTS D'INTÉGRATION ==========

    @Test
    void loadUserByUsername_ShouldCallRepositoryOnlyOnce() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        customUserDetailsService.loadUserByUsername(email);

        // Then
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_WithValidUser_ShouldReturnSpringSecurityUserInstance() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isInstanceOf(org.springframework.security.core.userdetails.User.class);
        assertThat(userDetails.getClass().getName())
                .isEqualTo("org.springframework.security.core.userdetails.User");

        verify(userRepository).findByEmail(email);
    }

    // ========== TESTS DE CAS LIMITES ==========

    @Test
    void loadUserByUsername_WithVeryLongEmail_ShouldWork() {
        // Given
        String longEmail = "a".repeat(100) + "@example.com";
        User userWithLongEmail = User.builder()
                .id(6L)
                .username("longemailuser")
                .email(longEmail)
                .password("password")
                .role("USER")
                .build();

        when(userRepository.findByEmail(longEmail)).thenReturn(Optional.of(userWithLongEmail));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(longEmail);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(longEmail);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");

        verify(userRepository).findByEmail(longEmail);
    }

    @Test
    void loadUserByUsername_WithSpecialCharactersInEmail_ShouldWork() {
        // Given
        String specialEmail = "test+special.email@sub-domain.example.com";
        User userWithSpecialEmail = User.builder()
                .id(7L)
                .username("specialuser")
                .email(specialEmail)
                .password("password")
                .role("USER")
                .build();

        when(userRepository.findByEmail(specialEmail)).thenReturn(Optional.of(userWithSpecialEmail));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(specialEmail);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(specialEmail);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");

        verify(userRepository).findByEmail(specialEmail);
    }

    // ========== TESTS DE VALIDATION DE SÉCURITÉ ==========

    @Test
    void loadUserByUsername_ShouldNotModifyOriginalUser() {
        // Given
        String email = "test@example.com";
        String originalPassword = testUser.getPassword();
        String originalRole = testUser.getRole();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        // Vérifier que l'utilisateur original n'a pas été modifié
        assertThat(testUser.getPassword()).isEqualTo(originalPassword);
        assertThat(testUser.getRole()).isEqualTo(originalRole);
        
        // Vérifier que UserDetails contient les bonnes informations
        assertThat(userDetails.getPassword()).isEqualTo(originalPassword);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_" + originalRole);

        verify(userRepository).findByEmail(email);
    }
}
