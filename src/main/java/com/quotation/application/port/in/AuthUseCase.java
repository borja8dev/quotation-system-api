package com.quotation.application.port.in;

import com.quotation.application.dto.AuthResponse;
import com.quotation.application.dto.LoginRequest;
import com.quotation.application.dto.RefreshRequest;
import com.quotation.application.dto.RegisterRequest;

public interface AuthUseCase {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(Long userId);
}
