package com.quotation.infrastructure.input.rest;

import com.quotation.application.dto.UserRequest;
import com.quotation.application.dto.UserResponse;
import com.quotation.application.port.in.UserUseCase;
import com.quotation.domain.exception.UserNotFoundException;
import com.quotation.infrastructure.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = UserController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCase userUseCase;

    @Test
    void createUser_WhenValidRequest_ThenReturns201() throws Exception {
        UserRequest request = validRequest();
        when(userUseCase.createUser(any(UserRequest.class))).thenReturn(userResponse());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void createUser_WhenInvalidRequest_ThenReturns400() throws Exception {
        UserRequest invalidRequest = UserRequest.builder().build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void getUserById_WhenUserExists_ThenReturns200() throws Exception {
        when(userUseCase.getUserById(1L)).thenReturn(userResponse());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getUserById_WhenUserNotFound_ThenReturns404() throws Exception {
        when(userUseCase.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllUsers_WhenUsersExist_ThenReturns200WithList() throws Exception {
        when(userUseCase.getAllUsers()).thenReturn(List.of(userResponse()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void updateUser_WhenValidRequest_ThenReturns200() throws Exception {
        UserRequest request = validRequest();
        when(userUseCase.updateUser(eq(1L), any(UserRequest.class))).thenReturn(userResponse());

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteUser_WhenUserExists_ThenReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WhenUserNotFound_ThenReturns404() throws Exception {
        doThrow(new UserNotFoundException(99L)).when(userUseCase).deleteUser(99L);

        mockMvc.perform(delete("/api/v1/users/99"))
                .andExpect(status().isNotFound());
    }

    private UserRequest validRequest() {
        return UserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("600123456")
                .build();
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("600123456")
                .regular(false)
                .build();
    }
}
