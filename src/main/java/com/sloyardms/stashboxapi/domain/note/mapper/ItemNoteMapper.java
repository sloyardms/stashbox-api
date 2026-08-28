package com.sloyardms.stashboxapi.domain.note.mapper;

import com.sloyardms.stashboxapi.domain.note.dto.request.CreateItemNoteRequest;
import com.sloyardms.stashboxapi.domain.note.dto.request.UpdateItemNoteRequest;
import com.sloyardms.stashboxapi.domain.note.dto.response.ItemNoteDetailResponse;
import com.sloyardms.stashboxapi.domain.note.model.ItemNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemNoteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "pinned", ignore = true)
    @Mapping(target = "files", ignore = true)
    ItemNote toEntity(CreateItemNoteRequest createItemNoteRequest);

    ItemNoteDetailResponse toDetailResponse(ItemNote itemNote);

    @Mapping(target = "removedFileIds", ignore = true)
    UpdateItemNoteRequest toUpdateRequest(ItemNote itemNote);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "files", ignore = true)
    void updateEntityFromDto(UpdateItemNoteRequest updateItemNoteRequest, @MappingTarget ItemNote itemNote);

}
