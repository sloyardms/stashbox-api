package com.sloyardms.stashboxapi.domain.user.controller;

import com.sloyardms.stashboxapi.domain.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashboxapi.domain.user.dto.UserProfileResponse;
import com.sloyardms.stashboxapi.domain.user.dto.UserSettingsResponse;
import com.sloyardms.stashboxapi.domain.user.service.UserService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/me")
    public ResponseEntity<UserProfileResponse> findOrCreate(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        UserProfileResponse response = userService.findOrCreate(authenticatedUser.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteSelf(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        userService.deleteAndSyncWithKeycloak(authenticatedUser.id());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> updateUserSettings(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                                   @Valid @RequestBody UpdateUserSettingsRequest request) {
        UserSettingsResponse response = userService.updateSettings(authenticatedUser.id(), request);
        return ResponseEntity.ok(response);
    }

}
