package com.sloyardms.stashboxapi.domain.stash.service;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.mapper.StashItemMapper;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroupSettings;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.domain.stash.repository.StashItemRepository;
import com.sloyardms.stashboxapi.domain.tag.model.Tag;
import com.sloyardms.stashboxapi.domain.tag.repository.TagRepository;
import com.sloyardms.stashboxapi.infrastructure.storage.service.FileStorageService;
import com.sloyardms.stashboxapi.shared.exception.FieldErrorDetail;
import com.sloyardms.stashboxapi.shared.exception.types.FieldValidationException;
import com.sloyardms.stashboxapi.shared.exception.types.FileStorageException;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceAlreadyExistException;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StashItemService {

    private final ItemGroupRepository itemGroupRepository;
    private final StashItemRepository stashItemRepository;
    private final TagRepository tagRepository;
    private final StashItemMapper stashItemMapper;
    private final FileStorageService fileStorageService;

    @Transactional(rollbackFor = Exception.class)
    public StashItemDetailResponse create(UUID userId, String groupSlug,
                                          CreateStashItemRequest createStashItemRequest, MultipartFile image) {
        ItemGroup itemGroup = itemGroupRepository.findBySlugAndUserId(groupSlug, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "slug", groupSlug));

        verifyRequiredFields(itemGroup, createStashItemRequest, image);
        verifyUniqueFields(itemGroup, createStashItemRequest);
        Set<Tag> itemTags = createTags(itemGroup, createStashItemRequest.getTags());

        StashItem stashItem = stashItemMapper.toEntity(createStashItemRequest);
        stashItem.setUser(itemGroup.getUser());
        stashItem.setGroup(itemGroup);
        stashItem.setTitleNormalized(SlugUtils.normalize(createStashItemRequest.getTitle()));
        stashItem.setUrlNormalized(SlugUtils.normalizeUrl(createStashItemRequest.getUrl()));
        stashItem.setTags(itemTags);

        stashItemRepository.save(stashItem);

        if (image != null && !image.isEmpty()) {
            try {
                UUID imageId = UUID.randomUUID();
                String savedImagePath = fileStorageService.uploadCover(userId, stashItem.getId(), imageId, image);
                stashItem.setImagePath(savedImagePath);
            } catch (IOException ex) {
                throw new FileStorageException("Failed to upload cover image", ex);
            }
        }

        return stashItemMapper.toDetailResponse(stashItem);
    }

    private Set<Tag> createTags(ItemGroup itemGroup, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new HashSet<>();
        }

        Map<String, String> slugToNameMap = tags.stream()
                .collect(Collectors.toMap(SlugUtils::slugify, name -> name, (a, b) -> a));
        Set<String> slugs = slugToNameMap.keySet();

        List<Tag> existingTags = tagRepository.findAllByUserIdAndGroupIdAndSlugIn(itemGroup.getUser().getId(),
                itemGroup.getId(), slugs);
        Set<String> existingSlugs = existingTags.stream()
                .map(Tag::getSlug)
                .collect(Collectors.toSet());

        //identify new ones
        List<Tag> newTags = slugs.stream()
                .filter(slug -> !existingSlugs.contains(slug))
                .map(slug -> {
                    Tag tag = new Tag();
                    tag.setSlug(slug);
                    tag.setName(slugToNameMap.get(slug));
                    tag.setUser(itemGroup.getUser());
                    tag.setGroup(itemGroup);
                    return tag;
                })
                .toList();

        List<Tag> savedNewTags = tagRepository.saveAll(newTags);
        Set<Tag> allTags = new HashSet<>(existingTags);
        allTags.addAll(savedNewTags);

        return allTags;
    }

    private void verifyUniqueFields(ItemGroup itemGroup, CreateStashItemRequest createStashItemRequest) {
        ItemGroupSettings settings = itemGroup.getSettings();
        UUID groupId = itemGroup.getId();

        if (settings.isUniqueTitle()) {
            String titleNormalized = SlugUtils.normalize(createStashItemRequest.getTitle());
            if (stashItemRepository.existsByGroupIdAndTitleNormalized(groupId, titleNormalized)) {
                throw new ResourceAlreadyExistException("constraint.stash_items_unique_title");
            }
        }

        if (settings.isUniqueUrl()) {
            String urlNormalized = SlugUtils.normalizeUrl(createStashItemRequest.getUrl());
            if (stashItemRepository.existsByGroupIdAndUrlNormalized(groupId, urlNormalized)) {
                throw new ResourceAlreadyExistException("constraint.stash_items_unique_url");
            }
        }
    }

    private void verifyRequiredFields(ItemGroup itemGroup, CreateStashItemRequest request, MultipartFile image) {
        ItemGroupSettings settings = itemGroup.getSettings();
        List<FieldErrorDetail> errors = new ArrayList<>();

        if (settings.isRequiredTitle()) {
            if (!StringUtils.hasText(request.getTitle())) {
                errors.add(new FieldErrorDetail("title", "validation.notBlank"));
            }
        }

        if (settings.isRequiredUrl()) {
            if (!StringUtils.hasText(request.getUrl())) {
                errors.add(new FieldErrorDetail("url", "validation.notBlank"));
            }
        }

        if (settings.isRequiredImage() && (image == null || image.isEmpty())) {
            errors.add(new FieldErrorDetail("image", "validation.notBlank"));
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

    }

}
