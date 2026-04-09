package com.sloyardms.stashboxapi.domain.user.mapper;

import com.sloyardms.stashboxapi.domain.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashboxapi.domain.user.dto.UserSettingsResponse;
import com.sloyardms.stashboxapi.domain.user.model.UserSettings;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserSettingsMapper {

    UserSettingsResponse toResponse(UserSettings userSettings);

    UpdateUserSettingsRequest toUpdateRequest(UserSettings userSettings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateEntityFromDto(UpdateUserSettingsRequest request, @MappingTarget UserSettings userSettings);

}
