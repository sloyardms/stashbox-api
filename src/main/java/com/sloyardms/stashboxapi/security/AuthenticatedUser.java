package com.sloyardms.stashboxapi.security;

import java.util.UUID;

/**
 * Represents the authenticated user extracted from the JWT stored in the SecurityContext
 *
 * @param userId     Unique identifier of the user in the database
 * @param providerId Unique identifier of the user in the identity provider
 * @param username   Username associated with the authenticated user
 * @param email      Email address of the authenticated user
 */
public record AuthenticatedUser(UUID userId, UUID providerId, String username, String email) {

}
