package com.ticketmanagement.service;

import com.ticketmanagement.dto.request.LoginRequest;
import com.ticketmanagement.dto.request.RegisterRequest;
import com.ticketmanagement.dto.response.AuthResponse;
import com.ticketmanagement.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
