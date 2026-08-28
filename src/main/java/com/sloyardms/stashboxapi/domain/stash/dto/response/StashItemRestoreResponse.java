package com.sloyardms.stashboxapi.domain.stash.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StashItemRestoreResponse {

    private UUID id;
    private ItemGroupRefResponse group;

}
