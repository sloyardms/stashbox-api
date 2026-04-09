package com.sloyardms.stashboxapi.domain.stash.mapper;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.UpdateItemGroupRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupResponse;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = ItemGroupSettingsMapper.class)
public interface ItemGroupMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "defaultGroup",  ignore = true)
    ItemGroup toEntity(CreateItemGroupRequest createItemGroupRequest);

    ItemGroupResponse toResponse(ItemGroup itemGroup);

    ItemGroupDetailResponse toDetailResponse(ItemGroup itemGroup);

    UpdateItemGroupRequest toUpdateRequest(ItemGroup itemGroup);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "defaultGroup", ignore = true)
    void updateEntityFromDto(UpdateItemGroupRequest updateItemgroupRequest, @MappingTarget ItemGroup itemGroup);

}
