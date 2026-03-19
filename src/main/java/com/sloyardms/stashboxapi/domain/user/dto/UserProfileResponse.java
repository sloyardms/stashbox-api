package com.sloyardms.stashboxapi.domain.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String username;
    private UserSettingsResponse settings;
    private Instant createdAt;
    private Instant updatedAt;

}
