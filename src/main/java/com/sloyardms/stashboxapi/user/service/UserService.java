package com.sloyardms.stashboxapi.user.service;

import com.sloyardms.stashboxapi.user.model.User;
import com.sloyardms.stashboxapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "userIdByProviderId", key = "#providerId")
    public UUID resolveOrCreateUser(UUID providerId, String username, String email) {
        Optional<User> foundUser = userRepository.findByProviderId(providerId);

        if (foundUser.isPresent()) {
            return foundUser.get().getId();
        }

        User user = new User();
        user.setProviderId(providerId);
        user.setUsername(username);
        user.setEmail(email);

        UUID userId = userRepository.save(user).getId();

        log.info("User created for providerId: {}", providerId);

        return userId;
    }

}
