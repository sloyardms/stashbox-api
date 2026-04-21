package com.sloyardms.stashboxapi.domain.tag.controller;

import com.sloyardms.stashboxapi.domain.tag.dto.request.CreateTagRequest;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagDetailResponse;
import com.sloyardms.stashboxapi.domain.tag.service.TagService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.SortableFields;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/item-groups/{groupId}/tags")
public class ItemGroupTagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagDetailResponse> createTag(
            @PathVariable UUID groupId,
            @RequestBody @Valid CreateTagRequest createTagRequest,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.create(authenticatedUser.id(), groupId, createTagRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TagCountResponse>> getTags(
            @PathVariable UUID groupId,
            @RequestParam(name = "search", required = false) String searchQuery,
            @SortableFields(
                    value = {"name", "createdAt", "updatedAt",
                            "itemCount", "lastUsed"},
                    defaultField = "itemCount",
                    defaultDirection = Sort.Direction.ASC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<TagCountResponse> responsePage = tagService.search(authenticatedUser.id(), groupId, searchQuery, pageable);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<TagDetailResponse> getTag(
            @PathVariable UUID groupId, @PathVariable UUID tagId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.findDetail(authenticatedUser.id(), groupId, tagId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<TagDetailResponse> patchTag(
            @PathVariable UUID groupId, @PathVariable UUID tagId,
            @RequestBody JsonNode body,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.patch(authenticatedUser.id(), groupId, tagId, body);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<TagDetailResponse> patchTag(
            @PathVariable UUID groupId, @PathVariable UUID tagId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        tagService.delete(authenticatedUser.id(), groupId, tagId);
        return ResponseEntity.noContent().build();
    }

}
