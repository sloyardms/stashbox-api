package com.sloyardms.stashboxapi.user.service;

import com.sloyardms.stashboxapi.common.cache.CacheNames;
import com.sloyardms.stashboxapi.common.exception.ResourceNotFoundException;
import com.sloyardms.stashboxapi.user.dto.request.UpdateUserSettingsRequest;
import com.sloyardms.stashboxapi.user.dto.response.UserProfileResponse;
import com.sloyardms.stashboxapi.user.dto.response.UserSummaryResponse;
import com.sloyardms.stashboxapi.user.mapper.UserMapper;
import com.sloyardms.stashboxapi.user.mapper.UserSettingsMapper;
import com.sloyardms.stashboxapi.user.model.User;
import com.sloyardms.stashboxapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;
    private final UserSettingsMapper userSettingsMapper;

    private final CacheManager cacheManager;

    /**
     * Resolves the application user ID associated with the given user provider ID.
     * If no user exists for the given user provider ID, a new user data is created using the provided username and
     * email.
     * Results are cached by the user provider ID to avoid repeated database lookups
     *
     * @param providerId the unique user ID from the identity provider (e.g., Keycloak)
     * @param username   the username from the identity provider
     * @param email      the email from the identity provider
     * @return the new user corresponding to the provider ID
     */
    @Cacheable(value = CacheNames.USER_ID_BY_PROVIDER_ID, key = "#providerId")
    public User findOrCreate(UUID providerId, String username, String email) {
        Optional<User> foundUser = userRepository.findByProviderId(providerId);

        if (foundUser.isPresent()) {
            return foundUser.get();
        }

        User newUser = new User();
        newUser.setProviderId(providerId);
        newUser.setUsername(username);
        newUser.setEmail(email);

        newUser = userRepository.save(newUser);

        log.info("User created for providerId: {}", providerId);

        return newUser;
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> findAllByQuery(String query, Pageable pageable) {
        Page<User> results = userRepository.findAllByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(query,
                query, pageable);
        return results.map(userMapper::toSummaryResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        userRepository.deleteById(id);

        Cache cache = cacheManager.getCache(CacheNames.USER_ID_BY_PROVIDER_ID);
        if(cache != null){

        }
        Objects.requireNonNull(cacheManager.getCache(CacheNames.USER_ID_BY_PROVIDER_ID)).evict(CacheNames.USER_ID_BY_PROVIDER_ID);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateSettings(UUID id, UpdateUserSettingsRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", id));
        userSettingsMapper.updateEntity(request, user.getSettings());

        user = userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }

}
