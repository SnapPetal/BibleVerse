package com.thonbecker.bibleverse;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BibleVerseApplication {

    @Bean
    public Function<Void, String> about() {
        return value -> new JSONStringer().object().key("status").value("healthy").key("version").value("1.0.0").endObject().toString();
    }
}
