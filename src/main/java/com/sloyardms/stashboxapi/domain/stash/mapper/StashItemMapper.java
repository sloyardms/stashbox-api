package com.sloyardms.stashboxapi.domain.stash.mapper;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.UpdateStashItemRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.tag.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ItemGroupMapper.class, TagMapper.class})
public interface StashItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "titleNormalized", ignore = true)
    @Mapping(target = "urlNormalized", ignore = true)
    @Mapping(target = "favorite", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "tags", ignore = true)
    StashItem toEntity(CreateStashItemRequest createStashItemRequest);

    @Mapping(target = "tags", ignore = true)
    StashItemDetailResponse toDetailResponse(StashItem stashItem);

    @Mapping(target = "tags", ignore = true)
    UpdateStashItemRequest toUpdateRequest(StashItem stashItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "titleNormalized", ignore = true)
    @Mapping(target = "urlNormalized", ignore = true)
    @Mapping(target = "favorite", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntityFromDto(UpdateStashItemRequest updateStashItemRequest, @MappingTarget StashItem stashItem);

}
