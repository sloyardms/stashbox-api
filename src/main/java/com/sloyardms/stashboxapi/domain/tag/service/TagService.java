package com.sloyardms.stashboxapi.domain.tag.service;

import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.domain.tag.dto.request.CreateTagRequest;
import com.sloyardms.stashboxapi.domain.tag.dto.request.UpdateTagRequest;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagDetailResponse;
import com.sloyardms.stashboxapi.domain.tag.mapper.TagMapper;
import com.sloyardms.stashboxapi.domain.tag.model.Tag;
import com.sloyardms.stashboxapi.domain.tag.projection.TagCountProjection;
import com.sloyardms.stashboxapi.domain.tag.projection.TagDetailProjection;
import com.sloyardms.stashboxapi.domain.tag.repository.TagRepository;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.service.JsonPatchService;
import com.sloyardms.stashboxapi.shared.utils.PageableUtils;
import com.sloyardms.stashboxapi.shared.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ItemGroupRepository itemGroupRepository;

    private final TagMapper tagMapper;
    private final JsonPatchService jsonPatchService;

    @Transactional(readOnly = true)
    public TagDetailResponse findDetail(UUID userId, String groupSlug, String tagSlug) {
        TagDetailProjection tagDetail = tagRepository.findTagDetail(userId, groupSlug, tagSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "slug", tagSlug));
        return tagMapper.toDetailResponse(tagDetail);
    }

    @Transactional(readOnly = true)
    public Page<TagCountResponse> search(UUID userId, String groupSlug, String searchQuery, Pageable pageable) {
        Map<String, String> sortFieldMappings = Map.of(
                "itemCount", "tu.item_count",
                "lastUsed", "tu.last_used",
                "createdAt", "created_at",
                "updatedAt", "updated_at"
        );
        Pageable mappedPageable = PageableUtils.remapSort(pageable, sortFieldMappings);
        String query = (searchQuery == null || searchQuery.isBlank()) ? null : searchQuery;

        Page<TagCountProjection> tags = tagRepository.findAllTagCount(userId, groupSlug, query, mappedPageable);
        return tags.map(tagMapper::toCountResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    public TagDetailResponse create(UUID userId, String groupSlug, CreateTagRequest createTagRequest) {
        ItemGroup group = itemGroupRepository.findBySlugAndUserId(groupSlug, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "slug", groupSlug));

        Tag newTag = tagMapper.toEntity(createTagRequest);
        newTag.setUser(userRepository.getReferenceById(userId));
        newTag.setGroup(group);
        newTag.setSlug(SlugUtils.slugify(newTag.getName()));

        newTag = tagRepository.save(newTag);
        return tagMapper.toDetailResponse(newTag);
    }

    @Transactional(rollbackFor = Exception.class)
    public TagDetailResponse patch(UUID userId, String groupSlug, String tagSlug, JsonNode patch) {
        Tag targetTag = tagRepository.findBySlugAndUserIdAndGroupSlug(tagSlug, userId, groupSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "slug", tagSlug));

        String originalName = targetTag.getName();

        UpdateTagRequest updateDto = tagMapper.toUpdateRequest(targetTag);
        UpdateTagRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto, UpdateTagRequest.class);
        tagMapper.updateEntityFromDto(patchedDto, targetTag);

        // regenerate slug
        if (!originalName.equals(targetTag.getName())) {
            targetTag.setSlug(SlugUtils.slugify(targetTag.getName()));
        }

        targetTag = tagRepository.save(targetTag);
        return findDetail(userId, groupSlug, targetTag.getSlug());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, String groupSlug, String tagSlug) {
        int deleted = tagRepository.deleteBySlugAndUserIdAndGroupSlug(tagSlug, userId, groupSlug);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Tag", "slug", tagSlug);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMany(UUID userId, String groupSlug, List<UUID> tagsIds) {
        if (tagsIds == null || tagsIds.isEmpty()) {
            return;
        }
        tagRepository.deleteMany(userId, groupSlug, tagsIds);
    }

}
