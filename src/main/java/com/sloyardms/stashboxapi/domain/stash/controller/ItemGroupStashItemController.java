package com.sloyardms.stashboxapi.domain.stash.controller;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.service.StashItemService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.ValidSlug;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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

}
