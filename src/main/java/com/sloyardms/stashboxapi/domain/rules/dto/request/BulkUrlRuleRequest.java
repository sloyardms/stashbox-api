package com.sloyardms.stashboxapi.domain.rules.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BulkUrlRuleRequest {

    List<UUID> ids = new ArrayList<>();

}
