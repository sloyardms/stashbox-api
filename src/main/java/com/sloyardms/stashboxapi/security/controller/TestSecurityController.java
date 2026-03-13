package com.sloyardms.stashboxapi.security.controller;

import com.sloyardms.stashboxapi.security.AuthenticatedUserProvider;
import com.sloyardms.stashboxapi.security.annotation.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
@Profile("test")
public class TestSecurityController {

    private final AuthenticatedUserProvider authenticatedUserProvider;

    @GetMapping("/authenticated")
    public ResponseEntity<Map<String, Boolean>> authenticatedEndpoint() {
        System.out.println(authenticatedUserProvider.getAuthenticatedUser());
        return ResponseEntity.ok(Map.of("authenticated", true));
    }

    @GetMapping("/admin")
    @AdminOnly
    public ResponseEntity<Map<String, Boolean>> adminEndpoint() {
        System.out.println(authenticatedUserProvider.getAuthenticatedUser());
        return ResponseEntity.ok(Map.of("admin", true));
    }
}
