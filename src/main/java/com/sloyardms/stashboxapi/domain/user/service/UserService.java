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
import com.sloyardms.stashboxapi.infrastructure.storage.event.UserFolderDeleteEvent;
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
    public UserProfileResponse findOrCreate(UUID externalId) {
        User user = userRepository.findByExternalId(externalId)
                .orElseGet(() -> createUser(externalId));
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

    @Transactional(rollbackFor = Exception.class)
    public void deleteAndSyncWithKeycloak(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        keycloakClient.deleteUser(user.getId().toString());
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserFolderDeleteEvent(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserFolderDeleteEvent(id));
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
