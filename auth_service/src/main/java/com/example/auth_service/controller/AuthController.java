package com.example.auth_service.controller;

import com.example.auth_service.dto.UserRegistrationDto;
import com.example.auth_service.dto.LoginDto;
import com.example.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // đăng ký 1 acc mới
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDto dto) {
        userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Người dùng đã được tạo thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<java.util.Map<String, String>> login(@Valid @RequestBody LoginDto dto) {
        String token = userService.loginUser(dto);
        return ResponseEntity.ok(java.util.Map.of("access_token", token));
    }
}
