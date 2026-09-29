package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.UserRegistrationDto;
import com.example.auth_service.dto.LoginDto;
import com.example.auth_service.config.KeycloakConfig;
import com.example.auth_service.service.UserService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Keycloak keycloak;
    private final KeycloakConfig keycloakConfig;


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

        Response response = keycloak.realm(keycloakConfig.getRealm()).users().create(user);
        if (response.getStatus() != 201) {
            String errorMessage = response.readEntity(String.class);
            log.error("Create user error {}", errorMessage);
            throw new RuntimeException("Không thể tạo user trong Keycloak!");
        }
    }

    @Override
    public String loginUser(LoginDto dto) {
        try (Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakConfig.getServerUrl())
                .realm(keycloakConfig.getRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(keycloakConfig.getClientId())
                .clientSecret(keycloakConfig.getClientSecret())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .build()) {

            return userKeycloak.tokenManager()
                    .getAccessToken()
                    .getToken();
        } catch (Exception e) {
            log.warn("Login failed for user {}", dto.getUsername());
            throw new RuntimeException("Sai username hoặc password!", e);
        }
    }
}
