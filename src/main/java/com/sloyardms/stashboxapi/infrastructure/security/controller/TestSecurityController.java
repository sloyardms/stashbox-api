package com.sloyardms.stashboxapi.infrastructure.security.controller;

import com.sloyardms.stashboxapi.infrastructure.security.AuthenticatedUser;
import com.sloyardms.stashboxapi.infrastructure.security.annotation.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
@Profile("test")
public class TestSecurityController {

    @GetMapping("/authenticated")
    public ResponseEntity<Map<String, Boolean>> authenticatedEndpoint(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        System.out.println(authenticatedUser);
        return ResponseEntity.ok(Map.of("authenticated", true));
    }

    @GetMapping("/admin")
    @AdminOnly
    public ResponseEntity<Map<String, Boolean>> adminEndpoint(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        System.out.println(authenticatedUser);
        return ResponseEntity.ok(Map.of("admin", true));
    }
}
