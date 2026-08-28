package com.sloyardms.stashboxapi.domain.note.controller;

import com.sloyardms.stashboxapi.domain.note.dto.request.CreateItemNoteRequest;
import com.sloyardms.stashboxapi.domain.note.dto.response.ItemNoteDetailResponse;
import com.sloyardms.stashboxapi.domain.note.service.ItemNoteService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.SortableFields;
import com.sloyardms.stashboxapi.shared.validation.ValidSlug;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/item-groups/{groupSlug}/stash-items/{itemId}/item-notes")
public class ItemGroupItemNoteController {

    private final ItemNoteService itemNoteService;

    @GetMapping
    public ResponseEntity<Page<ItemNoteDetailResponse>> listNotes(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID itemId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        Page<ItemNoteDetailResponse> response = itemNoteService
                .list(authenticatedUser.id(), groupSlug, itemId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemNoteDetailResponse> create(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID itemId,
            @RequestPart("data") CreateItemNoteRequest createItemNoteRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        ItemNoteDetailResponse response = itemNoteService.create(
                authenticatedUser.id(), groupSlug, itemId, createItemNoteRequest, files);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "{itemNoteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemNoteDetailResponse> patch(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID itemId,
            @PathVariable UUID itemNoteId,
            @RequestPart("data") JsonNode body,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        ItemNoteDetailResponse response = itemNoteService.patch(
                authenticatedUser.id(), groupSlug, itemId, itemNoteId, body, files);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{itemNoteId}")
    public ResponseEntity<Void> delete(
            @PathVariable @ValidSlug String groupSlug,
            @PathVariable UUID itemId,
            @PathVariable UUID itemNoteId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser){
        itemNoteService.delete(
                authenticatedUser.id(), groupSlug, itemId, itemNoteId);
        return ResponseEntity.noContent().build();
    }

}
