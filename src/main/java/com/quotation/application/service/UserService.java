package com.quotation.application.service;

import com.quotation.application.dto.UserRequest;
import com.quotation.application.dto.UserResponse;
import com.quotation.application.port.in.UserUseCase;
import com.quotation.application.port.out.UserRepositoryPort;
import com.quotation.domain.entity.User;
import com.quotation.domain.exception.DuplicateEmailException;
import com.quotation.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepositoryPort.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        return toResponse(userRepositoryPort.save(toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepositoryPort.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existing.getEmail().equals(request.getEmail())
                && userRepositoryPort.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setCompanyName(request.getCompanyName());
        existing.setRegular(request.isRegular());

        return toResponse(userRepositoryPort.save(existing));
    }

    @Override
    public void deleteUser(Long id) {
        userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepositoryPort.deleteById(id);
    }

    private User toEntity(UserRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .regular(request.isRegular())
                .build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .companyName(user.getCompanyName())
                .regular(user.isRegular())
                .build();
    }
}
