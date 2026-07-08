package com.sloyardms.stashboxapi.domain.stash.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReorderItemGroupsRequest {

    private List<UUID> orderedItemGroupIds;

}
