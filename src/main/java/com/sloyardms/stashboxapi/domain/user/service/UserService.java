package com.sloyardms.stashboxapi.domain.user.service;

import com.sloyardms.stashboxapi.domain.stash.service.ItemGroupService;
import com.sloyardms.stashboxapi.domain.user.dto.request.UpdateUserSettingsRequest;
import com.sloyardms.stashboxapi.domain.user.dto.response.UserProfileResponse;
import com.sloyardms.stashboxapi.domain.user.dto.response.UserSettingsResponse;
import com.sloyardms.stashboxapi.domain.user.mapper.UserMapper;
import com.sloyardms.stashboxapi.domain.user.mapper.UserSettingsMapper;
import com.sloyardms.stashboxapi.domain.user.model.User;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import com.sloyardms.stashboxapi.infrastructure.cache.UserIdCacheStore;
import com.sloyardms.stashboxapi.infrastructure.security.client.KeycloakClient;
import com.sloyardms.stashboxapi.infrastructure.storage.event.UserHardDeleteEvent;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.service.JsonPatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ItemGroupService userGroupService;

    private final UserMapper userMapper;
    private final UserSettingsMapper userSettingsMapper;

    private final ApplicationEventPublisher eventPublisher;
    private final KeycloakClient keycloakClient;
    private final JsonPatchService jsonPatchService;
    private final UserIdCacheStore userIdCacheStore;

    @Transactional(rollbackFor = Exception.class)
    public UUID resolveInternalId(UUID externalId) {
        Optional<User> user = userRepository.findByExternalId(externalId);
        if(user.isPresent()){
            return user.get().getId();
        }

        User newUser = createUser(externalId);
        return newUser.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        return userMapper.toProfileResponse(user);
    }
    
    private User createUser(UUID externalId) {
        User newUser = new User();
        newUser.setExternalId(externalId);
        newUser = userRepository.save(newUser);

        userGroupService.createDefaultGroup(newUser);
        log.info("User created for external id: {}", externalId);
        return newUser;
    }

    /**
     * Deletes a user from Keycloak and the local database.
     *
     * Keycloak emits a USER-DELETE event after the external deletion. That event
     * is consumed by the backend and triggers delete(UUID), which is intentionally
     * idempotent because this method and external Keycloak actions can both result
     * in the same local deletion attempt.
     *
     * @param id the internal user id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAndSyncWithKeycloak(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        userRepository.delete(user);
        keycloakClient.deleteUser(user.getExternalId().toString());
        userIdCacheStore.evict(user.getExternalId());
        eventPublisher.publishEvent(new UserHardDeleteEvent(id));
    }

    /**
     * Handles user deletion events originating from external systems (keycloak).
     *
     * This method is idempotent because deletion events can be duplicated or can
     * arrive after the user has already been deleted locally.
     *
     * @param id the internal user id
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID id) {
        Optional<User> user = userRepository.findById(id);

        if(user.isEmpty()){
            log.debug("Ignoring duplicate user deletion event for already deleted user {}",id);
            return;
        }
        userRepository.delete(user.get());
        userIdCacheStore.evict(user.get().getExternalId());
        eventPublisher.publishEvent(new UserHardDeleteEvent(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public UserSettingsResponse updateSettings(UUID id, JsonNode patch) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));

        UpdateUserSettingsRequest updateDto = userSettingsMapper.toUpdateRequest(targetUser.getSettings());
        UpdateUserSettingsRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto,
                UpdateUserSettingsRequest.class);
        userSettingsMapper.updateEntityFromDto(patchedDto, targetUser.getSettings());

        userRepository.save(targetUser);

        return userSettingsMapper.toResponse(targetUser.getSettings());
    }

}
