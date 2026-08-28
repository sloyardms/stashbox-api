package com.sloyardms.stashboxapi.domain.stash.service;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.ImageAction;
import com.sloyardms.stashboxapi.domain.stash.dto.request.UpdateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemRestoreResponse;
import com.sloyardms.stashboxapi.domain.stash.mapper.StashItemMapper;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroupSettings;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.repository.ItemGroupRepository;
import com.sloyardms.stashboxapi.domain.stash.repository.StashItemRepository;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.domain.tag.mapper.TagMapper;
import com.sloyardms.stashboxapi.domain.tag.model.Tag;
import com.sloyardms.stashboxapi.domain.tag.repository.TagRepository;
import com.sloyardms.stashboxapi.infrastructure.storage.event.ImageHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.event.StashItemHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.event.StashItemsHardDeleteEvent;
import com.sloyardms.stashboxapi.infrastructure.storage.service.FileStorageService;
import com.sloyardms.stashboxapi.shared.exception.FieldErrorDetail;
import com.sloyardms.stashboxapi.shared.exception.types.FieldValidationException;
import com.sloyardms.stashboxapi.shared.exception.types.FileStorageException;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceAlreadyExistException;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.service.JsonPatchService;
import com.sloyardms.stashboxapi.shared.utils.FileValidator;
import com.sloyardms.stashboxapi.shared.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StashItemService {

    private final ItemGroupRepository itemGroupRepository;
    private final StashItemRepository stashItemRepository;
    private final TagRepository tagRepository;
    private final StashItemMapper stashItemMapper;
    private final FileStorageService fileStorageService;
    private final JsonPatchService jsonPatchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TagMapper tagMapper;

    /**
     * Creates a new StashItem in the given group, applying the group's field requirements
     * and uniqueness settings. If an image is provided, it's validated and stored as the
     * item's cover before the entity is persisted.
     *
     * @throws ResourceNotFoundException if the group doesn't exist or isn't owned by the user
     * @throws FieldValidationException if the image is invalid, or required/at-least-one-field rules are violated
     * @throws ResourceAlreadyExistException if the group enforces unique title/url and a conflict exists
     * @return StashItemDetailResponse the created stash item Dto
     */
    @Transactional(rollbackFor = Exception.class)
    public StashItemDetailResponse create(UUID userId, String groupSlug,
                                          CreateStashItemRequest createStashItemRequest, MultipartFile image) {

        validateImageIfPresent(image);

        // fetch the parent group
        ItemGroup itemGroup = itemGroupRepository.findBySlugAndUserId(groupSlug, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "slug", groupSlug));

        // create new object
        StashItem newStashItem = stashItemMapper.toEntity(createStashItemRequest);
        newStashItem.setId(UUID.randomUUID());
        newStashItem.setUser(itemGroup.getUser());
        newStashItem.setGroup(itemGroup);
        newStashItem.setTitleNormalized(SlugUtils.normalize(createStashItemRequest.getTitle()));
        newStashItem.setUrlNormalized(SlugUtils.normalizeUrl(createStashItemRequest.getUrl()));

        // run validations
        validateAtLeastOneFieldProvided(newStashItem, image);
        verifyRequiredFields(itemGroup, newStashItem, image, null);
        verifyUniqueFields(itemGroup, newStashItem);

        // create tags
        Set<Tag> itemTags = createTags(itemGroup, createStashItemRequest.getTags());
        newStashItem.setTags(itemTags);

        // save image cover
        String newImagePath = saveImageCover(userId, newStashItem.getId(), image);
        newStashItem.setImagePath(newImagePath);

        // save changes
        newStashItem = stashItemRepository.saveAndFlush(newStashItem);

        return toDetailResponse(newStashItem);
    }

    /**
     * Applies a JSON Merge Patch to an existing StashItem and optionally replaces its cover image.
     * Title/url normalization is only regenerated when those fields actually change.
     * If a new image is provided, the previous image is deleted after the transaction commits.
     *
     * @throws ResourceNotFoundException if the item doesn't exist or isn't owned by the user
     * @throws FieldValidationException if the image is invalid, or required/at-least-one-field rules are violated
     * @throws ResourceAlreadyExistException if the group enforces unique title/url and a conflict exists
     */
    @Transactional(rollbackFor = Exception.class)
    public StashItemDetailResponse patch(UUID userId, String groupSlug, UUID stashItemId, JsonNode patch, MultipartFile image){

        validateImageIfPresent(image);

        // fetch the target stash item and its parent group
        StashItem targetStashItem = stashItemRepository.findByIdAndUserIdAndGroupSlug(stashItemId, userId, groupSlug)
                .orElseThrow(() -> new ResourceNotFoundException("StashItem", "id", stashItemId));
        ItemGroup itemGroup = targetStashItem.getGroup();

        UpdateStashItemRequest patchedDto = applyPatchAndValidate(itemGroup, targetStashItem, patch, image);

        // save the stashitem and tags
        Set<Tag> tags = createTags(targetStashItem.getGroup(), patchedDto.getTags());
        targetStashItem.setTags(tags);
        targetStashItem = stashItemRepository.saveAndFlush(targetStashItem);

        applyImageAction(userId, targetStashItem, patchedDto.getImageAction(), image);

        return toDetailResponse(targetStashItem);
    }

    @Transactional(rollbackFor = Exception.class)
    public void moveItem(UUID userId, String groupSlug, UUID stashItemId, JsonNode body,
                         String targetGroupSlug, MultipartFile image){
        if(groupSlug.equals(targetGroupSlug)){
            throw new FieldValidationException("targetGroup", "validation.same_group");
        }

        validateImageIfPresent(image);

        // fetch the target stash item and the target group
        StashItem targetStashItem = stashItemRepository.findByIdAndUserIdAndGroupSlug(stashItemId, userId, groupSlug)
                .orElseThrow(() -> new ResourceNotFoundException("StashItem", "id", stashItemId));
        ItemGroup targetGroup = itemGroupRepository.findBySlugAndUserId(targetGroupSlug, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemGroup", "id", targetGroupSlug));

        UpdateStashItemRequest patchedDto = applyPatchAndValidate(targetGroup, targetStashItem, body, image);

        // save the stashitem and tags
        Set<Tag> tags = createTags(targetGroup, patchedDto.getTags());
        targetStashItem.setGroup(targetGroup);
        targetStashItem.setTags(tags);
        targetStashItem = stashItemRepository.saveAndFlush(targetStashItem);

        applyImageAction(userId, targetStashItem, patchedDto.getImageAction(), image);

    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID stashItemId){
        StashItem targetStashItem = stashItemRepository.findByIdAndUserId(stashItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StashItem", "id", stashItemId));

        if(targetStashItem.getDeletedAt() == null){
            // Soft delete
            targetStashItem.setDeletedAt(Instant.now());
            stashItemRepository.save(targetStashItem);
        }else{
            stashItemRepository.delete(targetStashItem);
            applicationEventPublisher.publishEvent(new StashItemHardDeleteEvent(userId, stashItemId));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMany(UUID userId, List<UUID> stashItemIds){
        List<StashItem> stashItems = stashItemRepository.findAllByIdInAndUserId(stashItemIds, userId);

        List<StashItem> itemsToSoftDelete = new ArrayList<>();
        List<StashItem> itemsToHardDelete = new ArrayList<>();

        Instant now = Instant.now();

        for(StashItem stashItem : stashItems){
            if(stashItem.getDeletedAt() == null){
                stashItem.setDeletedAt(now);
                itemsToSoftDelete.add(stashItem);
            }else{
                itemsToHardDelete.add(stashItem);
            }
        }

        if(!itemsToSoftDelete.isEmpty()){
            stashItemRepository.saveAll(itemsToSoftDelete);
        }

        if(!itemsToHardDelete.isEmpty()){
            List<UUID> hardDeletedIds = itemsToHardDelete.stream()
                    .map(StashItem::getId)
                    .toList();

            stashItemRepository.deleteAll(itemsToHardDelete);
            applicationEventPublisher.publishEvent(new StashItemsHardDeleteEvent(userId, hardDeletedIds));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public long emptyTrash(UUID userId){
        List<StashItem> stashItems = stashItemRepository.findAllByUserIdAndDeletedAtNotNull(userId);

        long count = 0;
        if(!stashItems.isEmpty()){
            List<UUID> hardDeletedIds = stashItems.stream()
                    .map(StashItem::getId)
                    .toList();
            count = stashItemRepository.emptyTrash(userId);
            applicationEventPublisher.publishEvent(new StashItemsHardDeleteEvent(userId, hardDeletedIds));

        }
        return count;
    }

    @Transactional(readOnly = true)
    public long softDeletedCount(UUID userId){
        return stashItemRepository.countByUserIdAndDeletedAtIsNotNull(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(UUID userId, String groupSlug, UUID stashItemId){
        int result = stashItemRepository.toggleFavoriteMany(userId, groupSlug, List.of(stashItemId));
        if (result != 1) {
            throw new ResourceNotFoundException("StashItem", "id", stashItemId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void toggleFavoriteMany(UUID userId, String groupSlug, List<UUID> stashItemIds){
        stashItemRepository.toggleFavoriteMany(userId, groupSlug, stashItemIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public StashItemRestoreResponse restoreItem(UUID userId, UUID stashItemId){
        StashItem stashItem = stashItemRepository.findByIdAndUserId(stashItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StashItem", "id", stashItemId));

        stashItem.setDeletedAt(null);
        stashItemRepository.save(stashItem);

        return stashItemMapper.toRestoreResponse(stashItem);
    }

    // HELPER METHODS

    private UpdateStashItemRequest applyPatchAndValidate(ItemGroup validationGroup, StashItem targetStashItem,
                                                         JsonNode patch, MultipartFile image) {
        String originalTitle = targetStashItem.getTitle();
        String originalUrl = targetStashItem.getUrl();

        // apply patch
        UpdateStashItemRequest updateDto = stashItemMapper.toUpdateRequest(targetStashItem);
        UpdateStashItemRequest patchedDto = jsonPatchService.applyPatch(patch, updateDto, UpdateStashItemRequest.class);
        stashItemMapper.updateEntityFromDto(patchedDto, targetStashItem);

        // regenerate normalized title and url
        if(originalTitle!=null && !originalTitle.equals(targetStashItem.getTitle())) {
            targetStashItem.setTitleNormalized(SlugUtils.normalize(targetStashItem.getTitle()));
        }
        if(originalUrl!=null && !originalUrl.equals(targetStashItem.getUrl())) {
            targetStashItem.setUrlNormalized(SlugUtils.normalize(targetStashItem.getUrl()));
        }

        // run validations
        validateAtLeastOneFieldProvided(targetStashItem, image);
        verifyRequiredFields(validationGroup, targetStashItem, image, patchedDto.getImageAction());
        verifyUniqueFields(validationGroup, targetStashItem);

        return patchedDto;
    }

    private void applyImageAction(UUID userId, StashItem stashItem, ImageAction action, MultipartFile image) {
        // update imagePath
        switch(action){
            case REMOVE ->{
                String previousImagePath = stashItem.getImagePath();

                if (previousImagePath != null) {
                    stashItem.setImagePath(null);
                    stashItemRepository.save(stashItem);

                    applicationEventPublisher.publishEvent(
                            new ImageHardDeleteEvent(previousImagePath)
                    );
                }
            }
            case REPLACE -> {
                if (image == null || image.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Image is required when imageAction is REPLACE"
                    );
                }
                String previousImagePath = stashItem.getImagePath();
                String newImagePath = saveImageCover(userId, stashItem.getId(), image);
                stashItem.setImagePath(newImagePath);
                stashItem = stashItemRepository.save(stashItem);

                // delete old image
                if(previousImagePath != null && !previousImagePath.equals(stashItem.getImagePath())){
                    applicationEventPublisher.publishEvent(new ImageHardDeleteEvent(previousImagePath));
                }
            }
        }
    }

    /**
     * Builds a detailed response DTO for a stash item, including tag usage information.
     * The base response is created from the stash item entity using the mapper.
     * Tags are intentionally excluded from the entity mapping because the response
     * requires additional computed data (the number of items using each tag).
     * Tags are fetched separately with their usage counts and added to the response.
     *
     * @param item the stash item entity to convert into detail response
     * @return a detailed stash item response containing the information and tags with usage counts
     */
    private StashItemDetailResponse toDetailResponse(StashItem item) {
        StashItemDetailResponse response =
                stashItemMapper.toDetailResponse(item);

        List<TagCountResponse> tags =
                tagRepository.findTagsWithCountForStashItem(item.getId())
                        .stream()
                        .map(tagMapper::toCountResponse)
                        .toList();
        response.setTags(tags);
        return response;
    }

    private String saveImageCover(UUID userId, UUID stashItemId, MultipartFile image){
        if(image != null && !image.isEmpty()){
            try{
                UUID newImageId = UUID.randomUUID();
                return fileStorageService.uploadCover(userId, stashItemId, newImageId, image);
            } catch(IOException | IllegalStateException ex) {
                throw new FileStorageException("Failed to upload cover image", ex);
            }
        }
        return null;
    }

    private void validateImageIfPresent(MultipartFile image){
        if(image == null || image.isEmpty()) return;

        if(!FileValidator.isImage(image)){
            throw new FieldValidationException("image", "validation.invalid_image");
        }
    }

    /**
     * Resolves the given tag names to existing Tags in the group, creating any that don't
     * exist yet. Slug deduplicates tag names (case/format-insensitive).
     *
     * @return the full set of resolved Tags, existing and newly created
     */
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

    private void validateAtLeastOneFieldProvided(StashItem stashItem, MultipartFile image) {
        if (stashItem.getTitle() == null &&
                stashItem.getUrl() == null &&
                stashItem.getDescription() == null &&
                (image == null || image.isEmpty())) {
            throw new FieldValidationException(List.of(
                    new FieldErrorDetail("title", "validation.at_least_one_field_required"),
                    new FieldErrorDetail("url", "validation.at_least_one_field_required"),
                    new FieldErrorDetail("description", "validation.at_least_one_field_required"),
                    new FieldErrorDetail("image", "validation.at_least_one_field_required")
            ));
        }
    }

    /**
     * Enforces the group's unique-title/unique-url settings, if enabled.
     * Excludes the item's own id from the conflict check, so patching an item
     * without changing title/url doesn't self-conflict; a null id (create flow)
     * checks group-wide instead
     *
     * @param itemGroup The group to which the item belongs
     * @param stashItem The item to validate
     * @throws ResourceAlreadyExistException if a conflicting title or url exists in the group
     */
    private void verifyUniqueFields(ItemGroup itemGroup, StashItem stashItem) {
        ItemGroupSettings settings = itemGroup.getSettings();
        UUID groupId = itemGroup.getId();

        if (settings.isUniqueTitle()) {
            String titleNormalized = SlugUtils.normalize(stashItem.getTitle());
            if(stashItem.getId() != null) {
                if (stashItemRepository.existsByGroupIdAndTitleNormalizedAndIdNot(groupId, titleNormalized, stashItem.getId())) {
                    throw new ResourceAlreadyExistException("stash_items_group_id_title_unique");
                }
            }else{
                if (stashItemRepository.existsByGroupIdAndTitleNormalized(groupId, titleNormalized)) {
                    throw new ResourceAlreadyExistException("stash_items_group_id_title_unique");
                }
            }
        }

        if (settings.isUniqueUrl()) {
            String urlNormalized = SlugUtils.normalizeUrl(stashItem.getUrl());
            if(stashItem.getId() != null){
                if (stashItemRepository.existsByGroupIdAndUrlNormalizedAndIdNot(groupId, urlNormalized, stashItem.getId())) {
                    throw new ResourceAlreadyExistException("stash_items_group_id_url_unique");
                }
            }else{
                if (stashItemRepository.existsByGroupIdAndUrlNormalized(groupId, urlNormalized)) {
                    throw new ResourceAlreadyExistException("stash_items_group_id_url_unique");
                }
            }
        }
    }

    /**
     * Enforces the group's per-field "required" settings (title, url, image) that go
     * beyond the generic at-least-one-field check.
     *
     * @param itemGroup The group to which the item belongs
     * @param stashItem The item to validate
     * @param imageAction The action to apply to the image
     * @throws FieldValidationException aggregating all violated required fields
     */
    private void verifyRequiredFields(ItemGroup itemGroup, StashItem stashItem, MultipartFile image, ImageAction imageAction) {
        ItemGroupSettings settings = itemGroup.getSettings();
        List<FieldErrorDetail> errors = new ArrayList<>();

        if (settings.isRequiredTitle()) {
            if (!StringUtils.hasText(stashItem.getTitle())) {
                errors.add(new FieldErrorDetail("title", "validation.notBlank"));
            }
        }

        if (settings.isRequiredUrl()) {
            if (!StringUtils.hasText(stashItem.getUrl())) {
                errors.add(new FieldErrorDetail("url", "validation.notBlank"));
            }
        }

        boolean hasNewImage = image != null && !image.isEmpty();
        boolean hasExistingImage = StringUtils.hasText(stashItem.getImagePath());
        boolean imageWillExist = hasNewImage || (ImageAction.KEEP.equals(imageAction) && hasExistingImage);

        if (settings.isRequiredImage() && !imageWillExist) {
            errors.add(new FieldErrorDetail("image", "validation.notBlank"));
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

}
