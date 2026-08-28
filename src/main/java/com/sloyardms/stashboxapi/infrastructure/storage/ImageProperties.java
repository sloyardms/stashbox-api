package com.sloyardms.stashboxapi.infrastructure.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.images")
public class ImageProperties {

    private int maxSize;
    private float outputQuality;

}
