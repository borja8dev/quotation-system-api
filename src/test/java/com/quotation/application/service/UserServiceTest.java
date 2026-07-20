package com.quotation.application.service;

import com.quotation.application.dto.UserRequest;
import com.quotation.application.dto.UserResponse;
import com.quotation.application.port.out.UserRepositoryPort;
import com.quotation.domain.entity.User;
import com.quotation.domain.exception.DuplicateEmailException;
import com.quotation.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_WhenEmailIsUnique_ThenReturnsUserResponse() {
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser());

        UserResponse result = userService.createUser(validRequest());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailIsDuplicate_ThenThrowsDuplicateEmailException() {
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(validRequest()))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void getUserById_WhenUserExists_ThenReturnsUserResponse() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(savedUser()));

        UserResponse result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
    }

    @Test
    void getUserById_WhenUserNotFound_ThenThrowsUserNotFoundException() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllUsers_WhenUsersExist_ThenReturnsListOfResponses() {
        when(userRepositoryPort.findAll()).thenReturn(List.of(savedUser(), savedUser()));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteUser_WhenUserExists_ThenDeletesSuccessfully() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(savedUser()));

        userService.deleteUser(1L);

        verify(userRepositoryPort).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserNotFound_ThenThrowsUserNotFoundException() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepositoryPort, never()).deleteById(any());
    }

    private UserRequest validRequest() {
        return UserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("600123456")
                .build();
    }

    private User savedUser() {
        return User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("600123456")
                .regular(false)
                .build();
    }
}
