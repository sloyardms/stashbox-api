package com.sloyardms.stashboxapi.domain.rules.controller;

import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleListResponse;
import com.sloyardms.stashboxapi.domain.rules.service.UrlRuleService;
import com.sloyardms.stashboxapi.infrastructure.security.dto.AuthenticatedUser;
import com.sloyardms.stashboxapi.shared.validation.SortableFields;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/url-rules")
public class UrlRuleController {

    private final UrlRuleService urlRuleService;

    @GetMapping("/search")
    public ResponseEntity<Page<UrlRuleListResponse>> search(
            @RequestParam(value = "q", required = false) String query,
            @SortableFields(
                    value = {"name", "domain", "active", "priority",
                            "lastMatchedAt", "createdAt", "updatedAt"
                            , "groupName"},
                    defaultField = "name",
                    defaultDirection = Sort.Direction.ASC
            ) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        Page<UrlRuleListResponse> response = urlRuleService.search(authenticatedUser.id(), query, pageable);
        return ResponseEntity.ok(response);
    }
}
