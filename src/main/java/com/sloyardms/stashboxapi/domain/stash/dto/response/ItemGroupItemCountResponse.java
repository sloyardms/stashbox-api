package com.sloyardms.stashboxapi.domain.stash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemGroupItemCountResponse {

    private Long deletedItemCount;
    private Long activeItemCount;

}
