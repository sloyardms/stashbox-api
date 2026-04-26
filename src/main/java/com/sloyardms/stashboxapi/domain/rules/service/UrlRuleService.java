package com.sloyardms.stashboxapi.domain.rules.service;

import com.sloyardms.stashboxapi.domain.rules.dto.request.CreateUrlRuleRequest;
import com.sloyardms.stashboxapi.domain.rules.dto.request.UpdateUrlRuleRequest;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleDetailResponse;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleListResponse;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleSummaryResponse;
import com.sloyardms.stashboxapi.domain.rules.mapper.UrlRuleMapper;
import com.sloyardms.stashboxapi.domain.rules.model.UrlRule;
import com.sloyardms.stashboxapi.domain.rules.projection.UrlRuleListProjection;
import com.sloyardms.stashboxapi.domain.rules.repository.UrlRuleRepository;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.service.JsonPatchService;
import com.sloyardms.stashboxapi.shared.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UrlRuleService {

    private static final Map<String, String> SORT_FIELD_MAPPINGS = Map.of(
            "groupName", "ig.name"
    );

    private final UrlRuleRepository urlRuleRepository;
    private final ItemGroupRepository itemGroupRepository;
    private final UserRepository userRepository;

    private final UrlRuleMapper urlRuleMapper;
    private final JsonPatchService jsonPatchService;

    @Transactional(readOnly = true)
    public UrlRuleDetailResponse findById(UUID userId, UUID groupId, UUID ruleId) {
        UrlRule result = urlRuleRepository.findWithGroupByIdAndUserIdAndGroupId(ruleId, userId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("UrlRule", "Id", ruleId));
        return urlRuleMapper.toDetailResponse(result);
    }

    @Transactional(readOnly = true)
    public List<UrlRuleSummaryResponse> searchByGroupAndDomain(UUID userId, UUID groupId, String domain) {
        List<UrlRule> result = urlRuleRepository.findActiveByDomain(userId, groupId, domain);
        return result.stream().map(urlRuleMapper::toSummaryResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<UrlRuleListResponse> search(UUID userId, String searchQuery, Pageable pageable) {
        Pageable mappedPageable = PageableUtils.remapSort(pageable, SORT_FIELD_MAPPINGS);
        String query = (searchQuery == null || searchQuery.isBlank()) ? null : searchQuery;

        Page<UrlRuleListProjection> result = urlRuleRepository.search(userId, query, mappedPageable);
        return result.map(urlRuleMapper::toListResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    public UrlRuleDetailResponse create(UUID userId, UUID groupId, CreateUrlRuleRequest createUrlRuleRequest) {
        if (!itemGroupRepository.existsByIdAndUserId(groupId, userId)) {
            throw new ResourceNotFoundException("ItemGroup", "Id", groupId);
        }

        UrlRule newRule = urlRuleMapper.toEntity(createUrlRuleRequest);
        newRule.setUser(userRepository.getReferenceById(userId));
        newRule.setGroup(itemGroupRepository.getReferenceById(groupId));

        newRule = urlRuleRepository.save(newRule);
        return urlRuleMapper.toDetailResponse(newRule);
    }

    @Transactional(rollbackFor = Exception.class)
    public UrlRuleDetailResponse patch(UUID userId, UUID groupId, UUID urlRuleId, JsonNode patch) {
        UrlRule targetRule = urlRuleRepository.findByIdAndUserIdAndGroupId(urlRuleId, userId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("UrlRule", "Id", urlRuleId));

        UpdateUrlRuleRequest updateDto = urlRuleMapper.toUpdateRequest(targetRule);
        UpdateUrlRuleRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto, UpdateUrlRuleRequest.class);
        urlRuleMapper.updateEntityFromDto(patchedDto, targetRule);

        if (patchedDto.getGroupId() != null && !patchedDto.getGroupId().equals(groupId)) {
            if (!itemGroupRepository.existsByIdAndUserId(patchedDto.getGroupId(), userId)) {
                throw new ResourceNotFoundException("ItemGroup", "Id", patchedDto.getGroupId());
            }
            targetRule.setGroup(itemGroupRepository.getReferenceById(patchedDto.getGroupId()));
        }

        targetRule = urlRuleRepository.save(targetRule);
        return urlRuleMapper.toDetailResponse(targetRule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID groupId, UUID urlRuleId) {
        int deleted = urlRuleRepository.deleteByIdAndUserIdAndGroupId(urlRuleId, userId, groupId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("UrlRule", "Id", urlRuleId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateLastMatched(UUID userId, UUID groupId, UUID urlRuleId) {
        int modified = urlRuleRepository.updateLastMatched(urlRuleId, userId, groupId);
        if (modified == 0) {
            throw new ResourceNotFoundException("UrlRule", "Id", urlRuleId);
        }
    }

}
