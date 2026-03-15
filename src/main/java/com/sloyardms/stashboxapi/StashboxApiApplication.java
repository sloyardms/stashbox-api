package com.sloyardms.stashboxapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StashboxApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StashboxApiApplication.class, args);
    }

}
