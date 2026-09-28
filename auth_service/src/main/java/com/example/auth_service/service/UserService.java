package com.example.auth_service.service;

import com.example.auth_service.dto.UserRegistrationDto;

public interface UserService {
    void createUser(UserRegistrationDto dto);

}
