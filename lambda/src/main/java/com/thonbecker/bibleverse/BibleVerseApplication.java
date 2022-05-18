package com.thonbecker.bibleverse;

import org.json.JSONStringer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.internal.Function;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BibleVerseApplication {
    public static void main(String[] args) {
        SpringApplication.run(BibleVerseApplication.class, args);
    }

    @Bean
    public Function<String, String> about() {
        return value -> new JSONStringer().object().key("status").value("healthy").key("version").value("1.0.0").endObject().toString();
    }
}
