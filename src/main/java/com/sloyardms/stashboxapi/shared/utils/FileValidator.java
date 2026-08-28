package com.sloyardms.stashboxapi.shared.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FileValidator {

    private static final Tika TIKA = new Tika();

    public static boolean isImage(MultipartFile file) {
        return isImage(detectMimeType(file));
    }

    public static boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Detects the MIME type of a file from its content only (magic bytes), ignoring the
     * client-supplied filename and {@code Content-Type} header.
     *
     * @return the detected MIME type, or {@code null} if the file is empty or unreadable
     */
    public static String detectMimeType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try (InputStream inputStream = file.getInputStream()) {
            return TIKA.detect(inputStream);
        } catch (IOException e) {
            log.error("Error detecting file type: {}", e.getMessage(), e);
            return null;
        }
    }

}
