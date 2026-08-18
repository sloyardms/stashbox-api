package com.sloyardms.stashboxapi.domain.stash.controller;

import com.sloyardms.stashboxapi.domain.stash.dto.request.BulkStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemSummaryResponse;
import com.sloyardms.stashboxapi.domain.stash.service.StashItemSearchService;
import com.sloyardms.stashboxapi.domain.stash.service.StashItemService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.SortableFields;
import com.sloyardms.stashboxapi.shared.validation.ValidSlug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final StashItemSearchService stashItemSearchService;

    @GetMapping
    public ResponseEntity<Page<StashItemSummaryResponse>> listItems(
            @PathVariable @ValidSlug String groupSlug,
            @RequestParam(required = false) @Size(max = 250) String tags,
            @SortableFields(
                    value = {"title", "url", "description", "createdAt"},
                    defaultField = "createdAt",
                    defaultDirection = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<StashItemSummaryResponse> response = stashItemSearchService
                .list(authenticatedUser.id(), groupSlug, tags, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StashItemSummaryResponse>> searchItems(
            @PathVariable @ValidSlug String groupSlug,
            @RequestParam @NotBlank @Size(max = 100) String search,
            @RequestParam(required = false) @Size(max = 250) String tags,
            @SortableFields(
                    value = {"title", "url", "description", "createdAt", "relevance"},
                    defaultField = "relevance",
                    defaultDirection = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<StashItemSummaryResponse> response = stashItemSearchService
                .search(authenticatedUser.id(), groupSlug, search, tags, pageable);
        return ResponseEntity.ok(response);
    }

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

    @PutMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        stashItemService.toggleFavorite(authenticatedUser.id(), groupSlug, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bulk-favorite")
    public ResponseEntity<Void> toggleFavoriteBulk(
            @PathVariable @ValidSlug String groupSlug,
            @RequestBody BulkStashItemRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        stashItemService.toggleFavoriteMany(authenticatedUser.id(), groupSlug, request.getIds());
        return ResponseEntity.noContent().build();
    }

}
