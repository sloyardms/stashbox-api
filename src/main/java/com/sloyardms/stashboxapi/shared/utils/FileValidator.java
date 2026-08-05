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
        if(file == null || file.isEmpty()) {
            return false;
        }

        try(InputStream inputStream = file.getInputStream()){
            String mimeType = TIKA.detect(inputStream);
            return mimeType != null && mimeType.startsWith("image/");
        }catch (IOException e){
            log.error("Error validating file: {}", e.getMessage(), e);
            return false;
        }
    }

}
