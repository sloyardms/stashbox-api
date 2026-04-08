package com.sloyardms.stashboxapi.domain.stash.service;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.UpdateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupResponse;
import com.sloyardms.stashboxapi.domain.stash.mapper.ItemGroupMapper;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroupSettings;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.domain.user.model.User;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import com.sloyardms.stashboxapi.shared.exception.types.DefaultGroupDeletionNotAllowedException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemGroupService {

    private final ItemGroupRepository itemGroupRepository;
    private final ItemGroupMapper itemGroupMapper;
    private final UserRepository userRepository;
    private final JsonPatchService jsonPatchService;

    @Transactional(readOnly = true)
    public ItemGroupDetailResponse findById(UUID id, UUID userId) {
        ItemGroup targetGroup = itemGroupRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "Id", id));
        return itemGroupMapper.toDetailResponse(targetGroup);
    }

    @Transactional(readOnly = true)
    public Page<ItemGroupResponse> findAll(UUID userId, Pageable pageable) {
        Page<ItemGroup> groups = itemGroupRepository.findAllByUserId(userId, pageable);
        return groups.map(itemGroupMapper::toResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    public ItemGroupDetailResponse create(CreateItemGroupRequest createItemGroupRequest, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));

        int maxPosition = itemGroupRepository.findMaxPositionByUserId(userId);

        ItemGroup itemGroup = itemGroupMapper.toEntity(createItemGroupRequest);
        itemGroup.setUser(user);
        itemGroup.setSlug(SlugUtils.slugify(itemGroup.getName()));
        itemGroup.setPosition(maxPosition+1);
        itemGroup.setDefaultGroup(false);

        if (itemGroup.getSettings() == null) {
            itemGroup.setSettings(new ItemGroupSettings());
        }

        saveItemGroup(itemGroup);
        return itemGroupMapper.toDetailResponse(itemGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public ItemGroupDetailResponse patch(UUID id, JsonNode patch, UUID userId) {
        ItemGroup targetGroup = itemGroupRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "Id", id));

        String originalName = targetGroup.getName();

        UpdateItemGroupRequest updateDto = itemGroupMapper.toUpdateRequest(targetGroup);
        UpdateItemGroupRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto, UpdateItemGroupRequest.class);
        itemGroupMapper.updateEntityFromDto(patchedDto, targetGroup);

        // regenerate slug
        if (!originalName.equals(targetGroup.getName())) {
            targetGroup.setSlug(SlugUtils.slugify(targetGroup.getName()));
        }

        saveItemGroup(targetGroup);
        return itemGroupMapper.toDetailResponse(targetGroup);
    }

    private void saveItemGroup(ItemGroup itemGroup) {
        try {
            itemGroupRepository.saveAndFlush(itemGroup);
        } catch (DataIntegrityViolationException ex) {
            String message = ex.getMessage();
            if (message != null && message.contains("item_groups_slug_unique")) {
                throw new DuplicateResourceException("name", itemGroup.getName());
            }
            throw ex;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID id, UUID userId) {
        ItemGroup targetGroup = itemGroupRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "Id", id));

        if (targetGroup.isDefaultGroup()) {
            throw new DefaultGroupDeletionNotAllowedException();
        }
        itemGroupRepository.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefaultGroup(UUID id, UUID userId) {
        if (!itemGroupRepository.existsByIdAndUserId(id, userId)) {
            throw new ResourceNotFoundException("ItemGroup", "Id", id);
        }
        itemGroupRepository.clearDefaultGroup(userId);
        itemGroupRepository.setDefaultGroup(id, userId);
    }

    // Must be called within an active transaction (e.g. during user registration)
    @Transactional(propagation = Propagation.MANDATORY)
    public void createDefaultGroup(User user) {
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setUser(user);
        itemGroup.setName("Ungrouped");
        itemGroup.setSlug(SlugUtils.slugify(itemGroup.getName()));
        itemGroup.setPosition(0);
        itemGroup.setDefaultGroup(true);
        itemGroupRepository.save(itemGroup);

        log.info("Default item group created for user {}", user.getId());
    }

}
