package com.sloyardms.stashboxapi.infrastructure.storage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoredFile {

    private String storedFilename;
    private String relativeFilePath;
    private String fileExtension;
    private boolean image;

}
