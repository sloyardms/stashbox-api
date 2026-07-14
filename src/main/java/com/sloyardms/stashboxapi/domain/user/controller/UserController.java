package com.sloyardms.stashboxapi.domain.user.controller;

import com.sloyardms.stashboxapi.domain.user.dto.response.UserProfileResponse;
import com.sloyardms.stashboxapi.domain.user.dto.response.UserSettingsResponse;
import com.sloyardms.stashboxapi.domain.user.service.UserService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> findProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        UserProfileResponse response = userService.findById(authenticatedUser.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteSelf(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        userService.deleteAndSyncWithKeycloak(authenticatedUser.id());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> updateUserSettings(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody JsonNode body) {
        UserSettingsResponse response = userService.updateSettings(authenticatedUser.id(), body);
        return ResponseEntity.ok(response);
    }

}
