package com.sloyardms.stashboxapi.domain.tag.controller;

import com.sloyardms.stashboxapi.domain.tag.dto.request.CreateTagRequest;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagDetailResponse;
import com.sloyardms.stashboxapi.domain.tag.service.TagService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.SortableFields;
import com.sloyardms.stashboxapi.shared.validation.ValidSlug;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/item-groups/{groupSlug}/tags")
public class ItemGroupTagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagDetailResponse> createTag(
            @PathVariable @ValidSlug String groupSlug,
            @RequestBody @Valid CreateTagRequest createTagRequest,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.create(authenticatedUser.id(), groupSlug, createTagRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TagCountResponse>> getTags(
            @PathVariable @ValidSlug String groupSlug,
            @RequestParam(name = "search", required = false) String searchQuery,
            @SortableFields(
                    value = {"name", "createdAt", "updatedAt",
                            "itemCount", "lastUsed"},
                    defaultField = "itemCount",
                    defaultDirection = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<TagCountResponse> responsePage = tagService.search(authenticatedUser.id(), groupSlug, searchQuery,
                pageable);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{tagSlug}")
    public ResponseEntity<TagDetailResponse> getTag(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable @ValidSlug String tagSlug,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.findDetail(authenticatedUser.id(), groupSlug, tagSlug);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{tagSlug}")
    public ResponseEntity<TagDetailResponse> patchTag(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable @ValidSlug String tagSlug,
            @RequestBody JsonNode body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.patch(authenticatedUser.id(), groupSlug, tagSlug, body);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tagSlug}")
    public ResponseEntity<TagDetailResponse> patchTag(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable @ValidSlug String tagSlug,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        tagService.delete(authenticatedUser.id(), groupSlug, tagSlug);
        return ResponseEntity.noContent().build();
    }

}
