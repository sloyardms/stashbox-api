package com.sloyardms.stashboxapi.user.mapper;

import com.sloyardms.stashboxapi.user.dto.request.UpdateUserSettingsRequest;
import com.sloyardms.stashboxapi.user.dto.response.UserSettingsResponse;
import com.sloyardms.stashboxapi.user.model.UserSettings;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserSettingsMapper {

    UserSettingsResponse toResponse(UserSettings userSettings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserSettingsRequest request, @MappingTarget UserSettings userSettings);

}
