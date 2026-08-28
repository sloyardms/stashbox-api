package com.sloyardms.stashboxapi.domain.note.mapper;

import com.sloyardms.stashboxapi.domain.note.dto.response.NoteFileResponse;
import com.sloyardms.stashboxapi.domain.note.model.NoteFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteFileMapper {


    NoteFileResponse toResponse(NoteFile noteFile);

}
