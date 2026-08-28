package com.sloyardms.stashboxapi.infrastructure.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Constraints applied to note attachments before they are persisted or written to disk.
 * The MIME allow-list is matched against the <em>content-detected</em> type (Apache Tika),
 * never the client-supplied {@code Content-Type} header.
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.attachments")
public class AttachmentProperties {

    /** Maximum size of a single attachment. */
    private DataSize maxFileSize = DataSize.ofMegabytes(25);

    /** Maximum number of attachments a single note may hold (existing + newly uploaded). */
    private int maxFilesPerNote = 20;

    /** Content-detected MIME types that are allowed to be stored as note attachments. */
    private Set<String> allowedMimeTypes = new LinkedHashSet<>();

    public boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }
        String normalized = mimeType.toLowerCase(Locale.ROOT).trim();
        return allowedMimeTypes.stream()
                .map(type -> type.toLowerCase(Locale.ROOT).trim())
                .anyMatch(normalized::equals);
    }

}
