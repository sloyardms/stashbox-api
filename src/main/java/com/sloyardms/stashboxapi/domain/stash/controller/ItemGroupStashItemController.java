package com.sloyardms.stashboxapi.domain.stash.controller;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.UpdateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.service.StashItemService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.ValidSlug;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/item-groups/{groupSlug}/stash-items")
public class ItemGroupStashItemController {

    private final StashItemService stashItemService;

    @PostMapping
    public ResponseEntity<StashItemDetailResponse> create(
            @PathVariable @ValidSlug String groupSlug,
            @RequestPart("data") CreateStashItemRequest createStashItemRequest,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        StashItemDetailResponse response = stashItemService.create(authenticatedUser.id(), groupSlug,
                createStashItemRequest, image);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<StashItemDetailResponse> patch(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @RequestPart("data") JsonNode body,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        StashItemDetailResponse response = stashItemService.patch(authenticatedUser.id(), groupSlug,id, body, image);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        stashItemService.delete(authenticatedUser.id(), groupSlug, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        stashItemService.toggleFavorite(authenticatedUser.id(), groupSlug, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteImage(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        stashItemService.removeImage(authenticatedUser.id(), groupSlug, id);
        return ResponseEntity.noContent().build();
    }

}
