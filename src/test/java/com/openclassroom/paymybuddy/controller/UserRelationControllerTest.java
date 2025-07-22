package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserRelationControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private AuthService authService;
    @Mock
    private Model model;
    @Mock
    private RedirectAttributes redirectAttributes;
    @InjectMocks
    private UserRelationController controller;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    void showAddRelationForm_notAuthenticated_redirectsToLogin() {
        when(authService.getCurrentUser()).thenReturn(null);
        String view = controller.showAddRelationForm(model);
        assertEquals("redirect:/login", view);
    }

    @Test
    void showAddRelationForm_authenticated_returnsAddRelationsView() {
        when(authService.getCurrentUser()).thenReturn(user);
        String view = controller.showAddRelationForm(model);
        assertEquals("add-relations", view);
        verify(model).addAttribute(eq("user"), eq(user));
    }

    @Test
    void addRelation_notAuthenticated_redirectsToLogin() {
        when(authService.getCurrentUser()).thenReturn(null);
        String view = controller.addRelation("friend@example.com", redirectAttributes);
        assertEquals("redirect:/login", view);
    }

    @Test
    void addRelation_success_redirectsToUserTransactions() {
        when(authService.getCurrentUser()).thenReturn(user);
        doNothing().when(userService).addUserConnectionByIdentifier(eq(1L), eq("friend@example.com"));
        when(redirectAttributes.addFlashAttribute(eq("successMessage"), anyString())).thenReturn(redirectAttributes);
        String view = controller.addRelation("friend@example.com", redirectAttributes);
        assertEquals("redirect:/user-transactions/1", view);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), contains("succès"));
    }

    @Test
    void addRelation_userNotFound_redirectsToAddWithError() {
        when(authService.getCurrentUser()).thenReturn(user);
        doThrow(new EntityNotFoundException()).when(userService).addUserConnectionByIdentifier(eq(1L), eq("notfound@example.com"));
        when(redirectAttributes.addFlashAttribute(eq("errorMessage"), anyString())).thenReturn(redirectAttributes);
        String view = controller.addRelation("notfound@example.com", redirectAttributes);
        assertEquals("redirect:/user-relations/add", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("n'existe pas"));
    }

    @Test
    void addRelation_illegalArgument_redirectsToAddWithError() {
        when(authService.getCurrentUser()).thenReturn(user);
        doThrow(new IllegalArgumentException("Déjà ami")).when(userService).addUserConnectionByIdentifier(eq(1L), eq("already@example.com"));
        when(redirectAttributes.addFlashAttribute(eq("errorMessage"), anyString())).thenReturn(redirectAttributes);
        String view = controller.addRelation("already@example.com", redirectAttributes);
        assertEquals("redirect:/user-relations/add", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("Déjà ami"));
    }

    @Test
    void addRelation_unexpectedException_redirectsToAddWithGenericError() {
        when(authService.getCurrentUser()).thenReturn(user);
        doThrow(new RuntimeException("Erreur inconnue")).when(userService).addUserConnectionByIdentifier(eq(1L), eq("fail@example.com"));
        when(redirectAttributes.addFlashAttribute(eq("errorMessage"), anyString())).thenReturn(redirectAttributes);
        String view = controller.addRelation("fail@example.com", redirectAttributes);
        assertEquals("redirect:/user-relations/add", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("erreur inattendue"));
    }

    @Test
    void searchUsers_notAuthenticated_returns401() {
        when(authService.getCurrentUser()).thenReturn(null);
        ResponseEntity<List<User>> response = controller.searchUsers("bob");
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void searchUsers_authenticated_returnsUserList() {
        when(authService.getCurrentUser()).thenReturn(user);
        List<User> found = List.of(new User());
        when(userService.searchUsers("bob", 1L)).thenReturn(found);
        ResponseEntity<List<User>> response = controller.searchUsers("bob");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(found, response.getBody());
    }
}
