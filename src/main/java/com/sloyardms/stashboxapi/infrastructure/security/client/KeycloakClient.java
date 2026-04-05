package com.sloyardms.stashboxapi.infrastructure.security.client;

import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakClient {

    private final RealmResource realmResource;

    public void deleteUser(String userId) {
        try (Response response = realmResource.users().delete(userId)) {
            if (response.getStatus() == 404) {
                throw new ResourceNotFoundException("User", "Provider ID", userId);
            }
            if (response.getStatus() != 204) {
                throw new RuntimeException("Delete user from realm failed with status " + response.getStatus());
            }
        }
    }

}
