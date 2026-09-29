package com.example.auth_service.service;

import com.example.auth_service.dto.UserRegistrationDto;
import com.example.auth_service.dto.LoginDto;

public interface UserService {
    void createUser(UserRegistrationDto dto);

    String loginUser(LoginDto dto);
}
