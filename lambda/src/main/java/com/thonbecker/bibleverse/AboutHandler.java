package com.thonbecker.bibleverse;

import java.util.function.Function;

import org.json.JSONStringer;
import org.springframework.stereotype.Component;

@Component

public class AboutHandler implements Function<String, String> {

    @Override
    public String apply(String event) {
        return new JSONStringer().object().key("status").value("healthy").key("version").value("1.0.0").endObject().toString();
    }
}
