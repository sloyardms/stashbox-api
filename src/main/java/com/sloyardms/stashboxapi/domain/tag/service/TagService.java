package com.sloyardms.stashboxapi.domain.tag.service;

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
import com.sloyardms.stashboxapi.shared.exception.types.DuplicateResourceException;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.service.JsonPatchService;
import com.sloyardms.stashboxapi.shared.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

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
    public TagDetailResponse findDetail(UUID userId, UUID groupId, UUID tagId) {
        TagDetailProjection tagDetail = tagRepository.findTagDetail(userId, groupId, tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "Id", tagId));
        return tagMapper.toDetailResponse(tagDetail);
    }

    @Transactional(readOnly = true)
    public Page<TagCountResponse> search(UUID userId, UUID groupId, String searchQuery, Pageable pageable) {
        Page<TagCountProjection> tags = tagRepository.findAllTagCount(userId, groupId, searchQuery, pageable);
        return tags.map(tagMapper::toCountResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    public TagDetailResponse create(UUID userId, UUID groupId, CreateTagRequest createTagRequest) {
        if (!itemGroupRepository.existsByIdAndUserId(groupId, userId)) {
            throw new ResourceNotFoundException("ItemGroup", "Id", groupId);
        }

        Tag newTag = tagMapper.toEntity(createTagRequest);
        newTag.setUser(userRepository.getReferenceById(userId));
        newTag.setGroup(itemGroupRepository.getReferenceById(groupId));
        newTag.setSlug(SlugUtils.slugify(newTag.getName()));

        saveTag(newTag);
        return tagMapper.toDetailResponse(newTag);
    }

    @Transactional(rollbackFor = Exception.class)
    public TagDetailResponse patch(UUID userId, UUID groupId, UUID tagId, JsonNode patch) {
        Tag targetTag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "Id", tagId));

        String originalName = targetTag.getName();

        UpdateTagRequest updateDto = tagMapper.toUpdateRequest(targetTag);
        UpdateTagRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto, UpdateTagRequest.class);
        tagMapper.updateEntityFromDto(patchedDto, targetTag);

        // regenerate slug
        if (!originalName.equals(targetTag.getName())) {
            targetTag.setSlug(SlugUtils.slugify(targetTag.getName()));
        }

        saveTag(targetTag);
        return findDetail(userId, groupId, targetTag.getId());
    }

    private void saveTag(Tag tag) {
        try {
            tagRepository.saveAndFlush(tag);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message != null && message.contains("tags_slug_unique")) {
                throw new DuplicateResourceException("name", tag.getName());
            }
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID groupId, UUID tagId) {
        int deleted = tagRepository.deleteByIdAndUserIdAndGroupId(tagId, userId, groupId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Tag", "Id", tagId);
        }
    }

}
