package com.sloyardms.stashboxapi.user.mapper;

import com.sloyardms.stashboxapi.user.dto.response.UserProfileResponse;
import com.sloyardms.stashboxapi.user.dto.response.UserSummaryResponse;
import com.sloyardms.stashboxapi.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserSettingsMapper.class)
public interface UserMapper {

    UserProfileResponse toProfileResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);

}

