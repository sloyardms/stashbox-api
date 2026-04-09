package com.sloyardms.stashboxapi.domain.user.service;

import com.sloyardms.stashboxapi.domain.stash.service.ItemGroupService;
import com.sloyardms.stashboxapi.domain.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashboxapi.domain.user.dto.UserProfileResponse;
import com.sloyardms.stashboxapi.domain.user.dto.UserSettingsResponse;
import com.sloyardms.stashboxapi.domain.user.mapper.UserMapper;
import com.sloyardms.stashboxapi.domain.user.mapper.UserSettingsMapper;
import com.sloyardms.stashboxapi.domain.user.model.User;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
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

    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse findOrCreate(UUID id) {
        Optional<User> foundUser = userRepository.findById(id);
        if (foundUser.isPresent()) {
            return userMapper.toProfileResponse(foundUser.get());
        }

        User newUser = new User();
        newUser.setId(id);
        newUser = userRepository.save(newUser);

        log.info("User created for keycloak id: {}", id);
        userGroupService.createDefaultGroup(newUser);

        return userMapper.toProfileResponse(newUser);
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
