package com.sloyardms.stashboxapi.domain.tag.mapper;

import com.sloyardms.stashboxapi.domain.tag.dto.request.CreateTagRequest;
import com.sloyardms.stashboxapi.domain.tag.dto.request.UpdateTagRequest;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagDetailResponse;
import com.sloyardms.stashboxapi.domain.tag.model.Tag;
import com.sloyardms.stashboxapi.domain.tag.projection.TagCountProjection;
import com.sloyardms.stashboxapi.domain.tag.projection.TagDetailProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tag toEntity(CreateTagRequest createTagRequest);

    UpdateTagRequest toUpdateRequest(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateTagRequest updateTagRequest, @MappingTarget Tag tag);

    TagCountResponse toCountResponse(TagCountProjection tagCountProjection);

    TagDetailResponse toDetailResponse(TagDetailProjection tagDetailProjection);

    @Mapping(target = "itemCount", constant = "0")
    @Mapping(target = "lastUsed", ignore = true)
    TagDetailResponse toDetailResponse(Tag tag);

}
