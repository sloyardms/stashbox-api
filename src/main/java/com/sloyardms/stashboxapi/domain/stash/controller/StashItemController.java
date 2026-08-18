package com.sloyardms.stashboxapi.domain.stash.controller;

import com.sloyardms.stashboxapi.domain.stash.dto.request.BulkStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemSummaryResponse;
import com.sloyardms.stashboxapi.domain.stash.service.StashItemSearchService;
import com.sloyardms.stashboxapi.domain.stash.service.StashItemService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.SortableFields;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/stash-items")
public class StashItemController {

    private final StashItemSearchService stashItemSearchService;
    private final StashItemService stashItemService;

    @GetMapping("/{id}")
    public ResponseEntity<StashItemDetailResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        StashItemDetailResponse response = stashItemSearchService.findById(authenticatedUser.id(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<StashItemSummaryResponse>> listDeletedItems(
            @SortableFields(
                    value = {"title", "url", "description", "createdAt", "deletedAt"},
                    defaultField = "deletedAt",
                    defaultDirection = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<StashItemSummaryResponse> response = stashItemSearchService
                .listDeleted(authenticatedUser.id(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted/search")
    public ResponseEntity<Page<StashItemSummaryResponse>> searchDeletedItems(
            @RequestParam @NotBlank @Size(max = 100) String search,
            @SortableFields(
                    value = {"title", "url", "description", "createdAt", "relevance"},
                    defaultField = "relevance",
                    defaultDirection = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<StashItemSummaryResponse> response = stashItemSearchService
                .searchDeleted(authenticatedUser.id(), search, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        stashItemService.delete(authenticatedUser.id(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteBulk(
            @RequestBody BulkStashItemRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        stashItemService.deleteMany(authenticatedUser.id(),request.getIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deleted")
    public ResponseEntity<Long> emptyTrash(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        long count = stashItemService.emptyTrash(authenticatedUser.id());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/deleted/count")
    public ResponseEntity<Long> countDeleted(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        long count = stashItemService.softDeletedCount(authenticatedUser.id());
        return ResponseEntity.ok(count);
    }

}
