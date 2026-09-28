package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.UserRegistrationDto;
import com.example.auth_service.service.UserService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;


    @Override
    public void createUser(UserRegistrationDto dto) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        user.setCredentials(Collections.singletonList(credential));

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() != 201) {
            String errorMessage = response.readEntity(String.class);
            log.error("Create user error {}", errorMessage);
            throw new RuntimeException("Không thể tạo user trong Keycloak!");
        }
    }
}