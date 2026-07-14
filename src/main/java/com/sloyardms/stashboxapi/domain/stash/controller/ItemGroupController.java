package com.sloyardms.stashboxapi.domain.stash.controller;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupResponse;
import com.sloyardms.stashboxapi.domain.stash.service.ItemGroupService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.SortableFields;
import com.sloyardms.stashboxapi.shared.validation.ValidSlug;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/item-groups")
public class ItemGroupController {

    private final ItemGroupService itemGroupService;

    @GetMapping("/{slug}")
    public ResponseEntity<ItemGroupDetailResponse> getItemGroup(
            @PathVariable @ValidSlug String slug,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ItemGroupDetailResponse response = itemGroupService.findBySlug(authenticatedUser.id(), slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ItemGroupResponse>> getAllItemGroups(
            @SortableFields(
                    value = {"name", "position", "itemCount", "createdAt"},
                    defaultField = "position",
                    defaultDirection = Sort.Direction.ASC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        List<ItemGroupResponse> response = itemGroupService.findAll(authenticatedUser.id(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ItemGroupDetailResponse> create(
            @RequestBody @Valid CreateItemGroupRequest createItemGroupRequest,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ItemGroupDetailResponse response = itemGroupService.create(authenticatedUser.id(), createItemGroupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/{slug}")
    public ResponseEntity<ItemGroupDetailResponse> patch(
            @PathVariable @ValidSlug String slug,
            @RequestBody JsonNode body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ItemGroupDetailResponse response = itemGroupService.patch(authenticatedUser.id(), slug, body);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(
            @PathVariable @ValidSlug String slug,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        itemGroupService.delete(authenticatedUser.id(), slug);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{slug}/default")
    public ResponseEntity<Void> updateDefaultItemGroup(
            @PathVariable @ValidSlug String slug,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        itemGroupService.setDefaultGroup(authenticatedUser.id(), slug);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderItemGroups(
            @RequestBody List<UUID> orderedItemGroupIds,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        itemGroupService.reorder(authenticatedUser.id(), orderedItemGroupIds);
        return ResponseEntity.noContent().build();
    }

}
