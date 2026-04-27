package com.sloyardms.stashboxapi.domain.rules.controller;

import com.sloyardms.stashboxapi.domain.rules.dto.request.CreateUrlRuleRequest;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleDetailResponse;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleSummaryResponse;
import com.sloyardms.stashboxapi.domain.rules.service.UrlRuleService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.ValidSlug;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/item-groups/{groupSlug}/url-rules")
public class ItemGroupUrlRuleController {

    private final UrlRuleService urlRuleService;

    @GetMapping("/{id}")
    public ResponseEntity<UrlRuleDetailResponse> findById(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        UrlRuleDetailResponse response = urlRuleService.findById(authenticatedUser.id(), groupSlug, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UrlRuleSummaryResponse>> findByDomain(
            @PathVariable @ValidSlug String groupSlug,
            @RequestParam("domain") @NotBlank(message = "validations.notBlank") String domain,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        List<UrlRuleSummaryResponse> response = urlRuleService.searchByGroupAndDomain(authenticatedUser.id(), groupSlug,
                domain);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UrlRuleDetailResponse> create(
            @PathVariable @ValidSlug String groupSlug,
            @RequestBody @Valid CreateUrlRuleRequest createUrlRuleRequest,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        UrlRuleDetailResponse response = urlRuleService.create(authenticatedUser.id(), groupSlug, createUrlRuleRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UrlRuleDetailResponse> patch(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @RequestBody JsonNode body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        UrlRuleDetailResponse response = urlRuleService.patch(authenticatedUser.id(), groupSlug, id, body);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        urlRuleService.delete(authenticatedUser.id(), groupSlug, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/matched")
    public ResponseEntity<Void> updateMatched(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        urlRuleService.updateLastMatched(authenticatedUser.id(), groupSlug, id);
        return ResponseEntity.noContent().build();
    }

}
