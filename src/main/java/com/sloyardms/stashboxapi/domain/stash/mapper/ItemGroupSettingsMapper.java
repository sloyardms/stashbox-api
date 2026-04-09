package com.sloyardms.stashboxapi.domain.stash.mapper;

import com.sloyardms.stashboxapi.domain.stash.dto.request.CreateItemGroupSettingsRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.request.UpdateItemGroupSettingsRequest;
import com.sloyardms.stashboxapi.domain.stash.dto.response.ItemGroupSettingsResponse;
import com.sloyardms.stashboxapi.domain.stash.model.ItemGroupSettings;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ItemGroupSettingsMapper {

    ItemGroupSettingsResponse toResponse(ItemGroupSettings settings);

    UpdateItemGroupSettingsRequest toUpdateRequest(ItemGroupSettings settings);

    ItemGroupSettings toModel(CreateItemGroupSettingsRequest settings);

    ItemGroupSettings toModel(UpdateItemGroupSettingsRequest settings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UpdateItemGroupSettingsRequest updateItemGroupSettingsRequest,
                       @MappingTarget ItemGroupSettings settings);

}
