package com.sloyardms.stashboxapi.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record PendingUpload(
        UUID noteFileId,
        String relativeOutputPath,
        MultipartFile file,
        String outputFileExtension,
        boolean isImage){
}
