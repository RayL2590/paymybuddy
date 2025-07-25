package com.openclassroom.paymybuddy.service;

import com.openclassroom.paymybuddy.dto.RegisterDTO;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.repository.UserConnectionRepository;
import com.openclassroom.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConnectionRepository userConnectionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterDTO validRegisterDTO;
    private User existingUser;

    @BeforeEach
    void setUp() {
        validRegisterDTO = new RegisterDTO();
        validRegisterDTO.setUsername("testuser");
        validRegisterDTO.setEmail("test@example.com");
        validRegisterDTO.setPassword("password123");
        validRegisterDTO.setConfirmPassword("password123");

        existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .balance(BigDecimal.ZERO)
                .role("USER")
                .build();
    }

    @Test
    void registerUser_WithValidData_ShouldSucceed() {
        // Given
        when(userRepository.findByEmail(validRegisterDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRegisterDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = userService.registerUser(validRegisterDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(validRegisterDTO.getEmail());
        assertThat(result.getUsername()).isEqualTo(validRegisterDTO.getUsername());
        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getRole()).isEqualTo("USER");
        
        verify(userRepository).findByEmail(validRegisterDTO.getEmail());
        verify(passwordEncoder).encode(validRegisterDTO.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail(validRegisterDTO.getEmail())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validRegisterDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Un utilisateur avec cet email existe déjà");

        verify(userRepository).findByEmail(validRegisterDTO.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithNonMatchingPasswords_ShouldThrowException() {
        // Given
        validRegisterDTO.setConfirmPassword("differentPassword");
        when(userRepository.findByEmail(validRegisterDTO.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validRegisterDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Les mots de passe ne correspondent pas");

        verify(userRepository).findByEmail(validRegisterDTO.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(existingUser);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(userId);
    }

    @Test
    void isEmailAvailable_WithAvailableEmail_ShouldReturnTrue() {
        // Given
        String email = "new@example.com";
        Long currentUserId = 1L;
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.isEmailAvailable(email, currentUserId);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(email);
    }

    @Test
    void isEmailAvailable_WithTakenEmail_ShouldReturnFalse() {
        // Given
        String email = "taken@example.com";
        Long currentUserId = 1L;
        User otherUser = User.builder().id(2L).email(email).build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(otherUser));

        // When
        boolean result = userService.isEmailAvailable(email, currentUserId);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(email);
    }

    @Test
    void isEmailAvailable_WithCurrentUserEmail_ShouldReturnTrue() {
        // Given
        String email = "current@example.com";
        Long currentUserId = 1L;
        User currentUser = User.builder().id(currentUserId).email(email).build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(currentUser));

        // When
        boolean result = userService.isEmailAvailable(email, currentUserId);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(email);
    }

    @Test
    void isUsernameAvailable_WithAvailableUsername_ShouldReturnTrue() {
        // Given
        String username = "newuser";
        Long currentUserId = 1L;
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        boolean result = userService.isUsernameAvailable(username, currentUserId);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void isUsernameAvailable_WithTakenUsername_ShouldReturnFalse() {
        // Given
        String username = "takenuser";
        Long currentUserId = 1L;
        User otherUser = User.builder().id(2L).username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));

        // When
        boolean result = userService.isUsernameAvailable(username, currentUserId);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void isUsernameAvailable_WithCurrentUserUsername_ShouldReturnTrue() {
        // Given
        String username = "currentuser";
        Long currentUserId = 1L;
        User currentUser = User.builder().id(currentUserId).username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));

        // When
        boolean result = userService.isUsernameAvailable(username, currentUserId);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByUsername(username);
    }

    @Test
    void changePassword_WithValidData_ShouldSucceed() {
        // Given
        Long userId = 1L;
        String newPassword = "newPassword";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.changePassword(userId, newPassword);

        // Then
        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    void changePassword_WithNonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        String newPassword = "newPassword";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, newPassword))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeUsername_WithValidData_ShouldSucceed() {
        // Given
        Long userId = 1L;
        String newUsername = "newusername";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.changeUsername(userId, newUsername);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).findByUsername(newUsername);
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void changeUsername_WithTakenUsername_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String newUsername = "takenusername";
        User otherUser = User.builder().id(2L).username(newUsername).build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThatThrownBy(() -> userService.changeUsername(userId, newUsername))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ce nom d'utilisateur est déjà utilisé");

        verify(userRepository).findById(userId);
        verify(userRepository).findByUsername(newUsername);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeEmail_WithValidData_ShouldSucceed() {
        // Given
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.changeEmail(userId, newEmail);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void changeEmail_WithTakenEmail_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String newEmail = "taken@example.com";
        User otherUser = User.builder().id(2L).email(newEmail).build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThatThrownBy(() -> userService.changeEmail(userId, newEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cette adresse email est déjà utilisée");

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserBalance_WithValidAmount_ShouldSucceed() {
        // Given
        Long userId = 1L;
        BigDecimal newBalance = new BigDecimal("100.00");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUserBalance(userId, newBalance);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getBalance()).isEqualTo(newBalance);
    }

   @Test
    void updateUserBalance_WithNegativeAmount_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal negativeBalance = new BigDecimal("-10.00");

        // When & Then
        assertThatThrownBy(() -> userService.updateUserBalance(userId, negativeBalance))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La balance ne peut pas être inférieure à 0€");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserBalance_WithExcessiveAmount_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal excessiveBalance = new BigDecimal("15000.00");
        
        // When & Then
        assertThatThrownBy(() -> userService.updateUserBalance(userId, excessiveBalance))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La balance ne peut pas être supérieure à 10 000€");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== TESTS POUR addUserConnection ==========

    @Test
    void addUserConnection_WithValidData_ShouldSucceed() {
        // Given
        Long userId = 1L;
        String targetEmail = "target@example.com";
        User targetUser = User.builder()
                .id(2L)
                .email(targetEmail)
                .username("targetuser")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(targetEmail)).thenReturn(Optional.of(targetUser));
        when(userConnectionRepository.existsByUserAndConnection(existingUser, targetUser)).thenReturn(false);

        // When
        userService.addUserConnection(userId, targetEmail);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(targetEmail);
        verify(userConnectionRepository).existsByUserAndConnection(existingUser, targetUser);
        verify(userConnectionRepository).save(any());
    }

    @Test
    void addUserConnection_WithNonExistingCurrentUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        String targetEmail = "target@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.addUserConnection(userId, targetEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur courant non trouvé");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userConnectionRepository, never()).save(any());
    }

    @Test
    void addUserConnection_WithNonExistingTargetUser_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String targetEmail = "nonexistent@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(targetEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.addUserConnection(userId, targetEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur cible non trouvé");

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(targetEmail);
        verify(userConnectionRepository, never()).save(any());
    }

    @Test
    void addUserConnection_WithSameUser_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String targetEmail = "test@example.com"; // Same email as existingUser

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(targetEmail)).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.addUserConnection(userId, targetEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vous ne pouvez pas vous ajouter vous-même");

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(targetEmail);
        verify(userConnectionRepository, never()).save(any());
    }

    @Test
    void addUserConnection_WithExistingConnection_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String targetEmail = "target@example.com";
        User targetUser = User.builder().id(2L).email(targetEmail).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(targetEmail)).thenReturn(Optional.of(targetUser));
        when(userConnectionRepository.existsByUserAndConnection(existingUser, targetUser)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.addUserConnection(userId, targetEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cette connexion existe déjà");

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(targetEmail);
        verify(userConnectionRepository).existsByUserAndConnection(existingUser, targetUser);
        verify(userConnectionRepository, never()).save(any());
    }

    // ========== TESTS POUR searchUsers ==========

    @Test
    void searchUsers_WithValidSearchTerm_ShouldReturnUsers() {
        // Given
        String searchTerm = "john";
        Long currentUserId = 1L;
        List<User> expectedUsers = List.of(
                User.builder().id(2L).username("john_doe").email("john@example.com").build(),
                User.builder().id(3L).username("johny").email("johny@example.com").build()
        );

        when(userRepository.searchUsersExcludingCurrent(currentUserId, searchTerm)).thenReturn(expectedUsers);

        // When
        List<User> result = userService.searchUsers(searchTerm, currentUserId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedUsers);
        verify(userRepository).searchUsersExcludingCurrent(currentUserId, searchTerm);
    }

    @Test
    void searchUsers_WithEmptySearchTerm_ShouldReturnEmptyList() {
        // Given
        String searchTerm = "";
        Long currentUserId = 1L;

        // When
        List<User> result = userService.searchUsers(searchTerm, currentUserId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).searchUsersExcludingCurrent(anyLong(), anyString());
    }

    @Test
    void searchUsers_WithNullSearchTerm_ShouldReturnEmptyList() {
        // Given
        String searchTerm = null;
        Long currentUserId = 1L;

        // When
        List<User> result = userService.searchUsers(searchTerm, currentUserId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).searchUsersExcludingCurrent(anyLong(), anyString());
    }

    @Test
    void searchUsers_WithWhitespaceSearchTerm_ShouldReturnEmptyList() {
        // Given
        String searchTerm = "   ";
        Long currentUserId = 1L;

        // When
        List<User> result = userService.searchUsers(searchTerm, currentUserId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).searchUsersExcludingCurrent(anyLong(), anyString());
    }

    // ========== TESTS POUR findUserByEmailOrUsername ==========

    @Test
    void findUserByEmailOrUsername_WithExistingEmail_ShouldReturnUser() {
        // Given
        String identifier = "test@example.com";
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(existingUser));

        // When
        Optional<User> result = userService.findUserByEmailOrUsername(identifier);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(existingUser);
        verify(userRepository).findByEmail(identifier);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void findUserByEmailOrUsername_WithExistingUsername_ShouldReturnUser() {
        // Given
        String identifier = "testuser";
        
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(existingUser));

        // When
        Optional<User> result = userService.findUserByEmailOrUsername(identifier);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(existingUser);
        
        verify(userRepository).findByUsername(identifier);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void findUserByEmailOrUsername_WithNonExistingIdentifier_ShouldReturnEmpty() {
        // Given
        String identifier = "nonexistent";
        
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findUserByEmailOrUsername(identifier);

        // Then
        assertThat(result).isEmpty();
        
        verify(userRepository).findByUsername(identifier);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void findUserByEmailOrUsername_WithNonExistingEmail_ShouldReturnEmpty() {
        // Given
        String identifier = "notfound@example.com";
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findUserByEmailOrUsername(identifier);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(identifier);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void findUserByEmailOrUsername_WithEmailFormat_ShouldSearchByEmailOnly() {
        // Given
        String identifier = "user@domain.com";
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(existingUser));

        // When
        Optional<User> result = userService.findUserByEmailOrUsername(identifier);

        // Then
        assertThat(result).isPresent();
        verify(userRepository).findByEmail(identifier);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void findUserByEmailOrUsername_WithUsernameFormat_ShouldSearchByUsernameOnly() {
        // Given
        String identifier = "username123";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(existingUser));

        // When
        Optional<User> result = userService.findUserByEmailOrUsername(identifier);

        // Then
        assertThat(result).isPresent();
        verify(userRepository).findByUsername(identifier);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void addUserConnectionByIdentifier_WithExistingBidirectionalConnection_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String identifier = "target@example.com";
        User targetUser = User.builder().id(2L).email(identifier).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(targetUser));
        // ✅ Connexion existe dans un sens
        when(userConnectionRepository.existsByUserAndConnection(existingUser, targetUser)).thenReturn(false);
        when(userConnectionRepository.existsByUserAndConnection(targetUser, existingUser)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.addUserConnectionByIdentifier(userId, identifier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cette connexion existe déjà");

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(identifier);
        verify(userConnectionRepository).existsByUserAndConnection(existingUser, targetUser);
        verify(userConnectionRepository).existsByUserAndConnection(targetUser, existingUser);
        verify(userConnectionRepository, never()).save(any());
    }

    // ========== TESTS POUR addUserConnectionByIdentifier ==========

    @Test
    void addUserConnectionByIdentifier_WithValidIdentifier_ShouldSucceed() {
        // Given
        Long userId = 1L;
        String identifier = "target@example.com";
        User targetUser = User.builder()
                .id(2L)
                .email(identifier)
                .username("targetuser")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(targetUser));
        when(userConnectionRepository.existsByUserAndConnection(existingUser, targetUser)).thenReturn(false);
        when(userConnectionRepository.existsByUserAndConnection(targetUser, existingUser)).thenReturn(false);

        // When
        userService.addUserConnectionByIdentifier(userId, identifier);

        // Then
        verify(userRepository).findById(userId);
        verify(userConnectionRepository).existsByUserAndConnection(existingUser, targetUser);
        verify(userConnectionRepository).existsByUserAndConnection(targetUser, existingUser);
        
        verify(userConnectionRepository, times(2)).save(any());
    }

    @Test
    void addUserConnectionByIdentifier_WithNonExistingTargetUser_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String identifier = "nonexistent@example.com"; 

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.addUserConnectionByIdentifier(userId, identifier))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cet utilisateur n'existe pas");

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(identifier);
        
        verify(userRepository, never()).findByUsername(anyString());
        verify(userConnectionRepository, never()).save(any());
    }

    @Test
    void addUserConnectionByIdentifier_WithSameUser_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String identifier = "testuser"; 

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.addUserConnectionByIdentifier(userId, identifier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vous ne pouvez pas vous ajouter vous-même");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository).findByUsername(identifier);
        verify(userConnectionRepository, never()).save(any());
    }

    // ========== TESTS POUR adjustUserBalance ==========

    @Test
    void adjustUserBalance_WithAddOperation_ShouldSucceed() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("50.00");
        String operation = "ADD";
        existingUser.setBalance(new BigDecimal("100.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.adjustUserBalance(userId, amount, operation);

        // Then
        verify(userRepository, times(2)).findById(userId); // Une fois dans adjustUserBalance, une fois dans updateUserBalance
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getBalance()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void adjustUserBalance_WithSubtractOperation_ShouldSucceed() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("30.00");
        String operation = "SUBTRACT";
        existingUser.setBalance(new BigDecimal("100.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.adjustUserBalance(userId, amount, operation);

        // Then
        verify(userRepository, times(2)).findById(userId); // Une fois dans adjustUserBalance, une fois dans updateUserBalance
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getBalance()).isEqualTo(new BigDecimal("70.00"));
    }

    @Test
    void adjustUserBalance_WithNullAmount_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal amount = null;
        String operation = "ADD";

        // When & Then
        assertThatThrownBy(() -> userService.adjustUserBalance(userId, amount, operation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Le montant doit être supérieur à 0");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adjustUserBalance_WithZeroAmount_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.ZERO;
        String operation = "ADD";

        // When & Then
        assertThatThrownBy(() -> userService.adjustUserBalance(userId, amount, operation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Le montant doit être supérieur à 0");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adjustUserBalance_WithNegativeAmount_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("-10.00");
        String operation = "ADD";

        // When & Then
        assertThatThrownBy(() -> userService.adjustUserBalance(userId, amount, operation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Le montant doit être supérieur à 0");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adjustUserBalance_WithInvalidOperation_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("50.00");
        String operation = "INVALID";

        // When & Then
        assertThatThrownBy(() -> userService.adjustUserBalance(userId, amount, operation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Opération invalide. Utilisez 'ADD' ou 'SUBTRACT'");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adjustUserBalance_WithSubtractOperationResultingInNegative_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("150.00");
        String operation = "SUBTRACT";
        existingUser.setBalance(new BigDecimal("100.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.adjustUserBalance(userId, amount, operation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La balance ne peut pas être inférieure à 0€");

        verify(userRepository).findById(userId); // Une seule fois dans adjustUserBalance avant l'échec de validation
    }

    @Test
    void adjustUserBalance_WithAddOperationExceedingLimit_ShouldThrowException() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("5000.00");
        String operation = "ADD";
        existingUser.setBalance(new BigDecimal("9000.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.adjustUserBalance(userId, amount, operation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La balance ne peut pas être supérieure à 10 000€");

        verify(userRepository).findById(userId); // Une seule fois dans adjustUserBalance avant l'échec de validation
    }
}
