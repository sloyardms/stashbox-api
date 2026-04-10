package com.sloyardms.stashboxapi.domain.stash.controller;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupResponse;
import com.sloyardms.stashboxapi.domain.stash.service.ItemGroupService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/item-groups")
public class ItemGroupController {

    private final ItemGroupService itemGroupService;
    private final TagService tagService;

    @GetMapping("/{id}")
    public ResponseEntity<ItemGroupDetailResponse> getItemGroup(@PathVariable UUID id,
                                                                @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ItemGroupDetailResponse response = itemGroupService.findById(id, authenticatedUser.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ItemGroupResponse>> getAllItemGroups(
            @SortableFields(
                    value = {"name", "description", "position", "createdAt"},
                    defaultField = "position",
                    defaultDirection = Sort.Direction.ASC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<ItemGroupResponse> response = itemGroupService.findAll(authenticatedUser.id(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ItemGroupDetailResponse> create(
            @RequestBody @Valid CreateItemGroupRequest createItemGroupRequest,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ItemGroupDetailResponse response = itemGroupService.create(createItemGroupRequest, authenticatedUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<ItemGroupDetailResponse> patch(@PathVariable UUID id,
                                                         @RequestBody JsonNode body,
                                                         @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ItemGroupDetailResponse response = itemGroupService.patch(id, body, authenticatedUser.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        itemGroupService.delete(id, authenticatedUser.id());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<Void> updateDefaultItemGroup(@PathVariable UUID id,
                                                       @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        itemGroupService.setDefaultGroup(id, authenticatedUser.id());
        return ResponseEntity.noContent().build();
    }

    // TAGS ==========================================================================================================

    @PostMapping("/{groupId}/tags")
    public ResponseEntity<TagDetailResponse> createTag(@PathVariable UUID groupId,
                                                       @RequestBody CreateTagRequest createTagRequest,
                                                       @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.create(authenticatedUser.id(), groupId, createTagRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/tags")
    public ResponseEntity<Page<TagCountResponse>> getTags(@PathVariable UUID groupId,
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

    @GetMapping("/{groupId}/tags/{tagId}")
    public ResponseEntity<TagDetailResponse> getTag(@PathVariable UUID groupId, @PathVariable UUID tagId,
                                                    @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.findDetail(authenticatedUser.id(), groupId, tagId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{groupId}/tags/{tagId}")
    public ResponseEntity<TagDetailResponse> patchTag(@PathVariable UUID groupId, @PathVariable UUID tagId,
                                                      @RequestBody JsonNode body,
                                                      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TagDetailResponse response = tagService.patch(authenticatedUser.id(), groupId, tagId, body);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{groupId}/tags/{tagId}")
    public ResponseEntity<TagDetailResponse> patchTag(@PathVariable UUID groupId, @PathVariable UUID tagId,
                                                      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        tagService.delete(authenticatedUser.id(), groupId, tagId);
        return ResponseEntity.noContent().build();
    }

}
