package com.sloyardms.stashboxapi.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private UserSettingsResponse settings;
    private Instant createdAt;
    private Instant updatedAt;

}
