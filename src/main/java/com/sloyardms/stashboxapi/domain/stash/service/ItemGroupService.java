package com.sloyardms.stashboxapi.domain.stash.service;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.UpdateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupResponse;
import com.sloyardms.stashboxapi.domain.stash.mapper.ItemGroupMapper;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroupSettings;
import com.sloyardms.stashboxapi.domain.stash.projection.ItemGroupWithCount;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.domain.user.model.User;
import com.sloyardms.stashboxapi.domain.user.repository.UserRepository;
import com.sloyardms.stashboxapi.shared.exception.types.DefaultGroupDeletionNotAllowedException;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.service.JsonPatchService;
import com.sloyardms.stashboxapi.shared.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemGroupService {

    private final ItemGroupRepository itemGroupRepository;
    private final ItemGroupMapper itemGroupMapper;
    private final UserRepository userRepository;
    private final JsonPatchService jsonPatchService;

    @Transactional(readOnly = true)
    public ItemGroupDetailResponse findBySlug(UUID userId, String slug) {
        ItemGroup targetGroup = itemGroupRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "slug", slug));
        return itemGroupMapper.toDetailResponse(targetGroup);
    }

    @Transactional(readOnly = true)
    public List<ItemGroupResponse> findAll(UUID userId, Pageable pageable) {
        Pageable unpaged = Pageable.unpaged(pageable.getSort());
        Page<ItemGroupWithCount> groups = itemGroupRepository.findAllWithItemCountByUserId(userId, unpaged);
        List<ItemGroupWithCount> groupsList = groups.getContent();
        return groupsList.stream().map(itemGroupMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ItemGroupDetailResponse create(UUID userId, CreateItemGroupRequest createItemGroupRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));

        int maxPosition = itemGroupRepository.findMaxPositionByUserId(userId);

        ItemGroup itemGroup = itemGroupMapper.toEntity(createItemGroupRequest);
        itemGroup.setUser(user);
        itemGroup.setSlug(SlugUtils.slugify(itemGroup.getName()));
        itemGroup.setPosition(maxPosition + 1);
        itemGroup.setDefaultGroup(false);

        if(itemGroup.getSettings()==null){
            itemGroup.setSettings(new ItemGroupSettings());
        }

        itemGroup = itemGroupRepository.save(itemGroup);
        return itemGroupMapper.toDetailResponse(itemGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public ItemGroupDetailResponse patch(UUID userId, String slug, JsonNode patch) {
        ItemGroup targetGroup = itemGroupRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "slug", slug));

        String originalName = targetGroup.getName();

        UpdateItemGroupRequest updateDto = itemGroupMapper.toUpdateRequest(targetGroup);
        UpdateItemGroupRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto, UpdateItemGroupRequest.class);
        itemGroupMapper.updateEntityFromDto(patchedDto, targetGroup);

        // regenerate slug
        if (!originalName.equals(targetGroup.getName())) {
            targetGroup.setSlug(SlugUtils.slugify(targetGroup.getName()));
        }

        targetGroup = itemGroupRepository.save(targetGroup);
        return itemGroupMapper.toDetailResponse(targetGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, String slug) {
        ItemGroup targetGroup = itemGroupRepository.findBySlugAndUserId(slug, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "slug", slug));

        if (targetGroup.isDefaultGroup()) {
            throw new DefaultGroupDeletionNotAllowedException();
        }
        itemGroupRepository.deleteById(targetGroup.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefaultGroup(UUID userId, String slug) {
        if (!itemGroupRepository.existsBySlugAndUserId(slug, userId)) {
            throw new ResourceNotFoundException("ItemGroup", "slug", slug);
        }
        itemGroupRepository.clearDefaultGroup(userId);
        itemGroupRepository.setDefaultGroup(slug, userId);
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

    @Transactional(rollbackFor = Exception.class)
    public void reorder(UUID userId, List<UUID> orderedItemGroupIds) {
        List<ItemGroup> groups = itemGroupRepository.findAllById(orderedItemGroupIds);

        boolean allOwned = groups.stream().allMatch(g->g.getUser().getId().equals(userId));
        if(!allOwned||groups.size() != orderedItemGroupIds.size()){
            throw new AccessDeniedException("Invalid group ids for reorder");
        }

        Map<UUID, ItemGroup> byId = groups.stream()
                .collect(Collectors.toMap(ItemGroup::getId, g->g));

        for(int i = 0; i<orderedItemGroupIds.size(); i++){
            byId.get(orderedItemGroupIds.get(i)).setPosition(i);
        }

        itemGroupRepository.saveAll(groups);
    }

}
