package com.ticketmanagement.service;

import com.ticketmanagement.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();
}
