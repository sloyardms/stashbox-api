package com.sloyardms.stashboxapi.domain.rules.mapper;

import com.sloyardms.stashboxapi.domain.rules.dto.request.CreateUrlRuleRequest;
import com.sloyardms.stashboxapi.domain.rules.dto.request.UpdateUrlRuleRequest;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleDetailResponse;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleListResponse;
import com.sloyardms.stashboxapi.domain.rules.dto.response.UrlRuleSummaryResponse;
import com.sloyardms.stashboxapi.domain.rules.model.UrlRule;
import com.sloyardms.stashboxapi.domain.rules.projection.UrlRuleListProjection;
import com.sloyardms.stashboxapi.domain.stash.mapper.ItemGroupMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = ItemGroupMapper.class)
public interface UrlRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "lastMatchedAt", ignore = true)
    UrlRule toEntity(CreateUrlRuleRequest createUrlRuleRequest);

    @Mapping(target = "groupId", source = "group.id")
    UpdateUrlRuleRequest toUpdateRequest(UrlRule urlRule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "lastMatchedAt", ignore = true)
    void updateEntityFromDto(UpdateUrlRuleRequest updateUrlRuleRequest, @MappingTarget UrlRule urlRule);

    UrlRuleDetailResponse toDetailResponse(UrlRule urlRule);

    @Mapping(target = "group.id", source = "groupId")
    @Mapping(target = "group.name", source = "groupName")
    UrlRuleListResponse toListResponse(UrlRuleListProjection urlRuleListProjection);

    UrlRuleSummaryResponse toSummaryResponse(UrlRule urlRule);

}
