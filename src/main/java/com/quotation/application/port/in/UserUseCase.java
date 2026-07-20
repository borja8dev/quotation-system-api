package com.quotation.application.port.in;

import com.quotation.application.dto.UserRequest;
import com.quotation.application.dto.UserResponse;

import java.util.List;

public interface UserUseCase {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);
}
