package com.sloyardms.stashboxapi.infrastructure.security.dto;

import java.util.List;
import java.util.UUID;

/**
 * Represents the authenticated user extracted from the JWT stored in the SecurityContext
 *
 * @param id       Unique identifier of the user in the database
 * @param username Username associated with the authenticated user
 * @param email    Email address of the authenticated user
 * @param roles    Client roles of the authenticated user
 */
public record AuthenticatedUser(UUID id, String username, String email, List<String> roles) {

}
