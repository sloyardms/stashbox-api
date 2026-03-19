package com.sloyardms.stashboxapi.domain.user.mapper;

import com.sloyardms.stashboxapi.domain.user.dto.UserProfileResponse;
import com.sloyardms.stashboxapi.domain.user.dto.UserSummaryResponse;
import com.sloyardms.stashboxapi.domain.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserSettingsMapper.class)
public interface UserMapper {

    UserProfileResponse toProfileResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);

}

