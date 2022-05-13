package com.thonbecker.bibleverse;

import java.util.function.Function;

import org.json.JSONStringer;
import org.springframework.stereotype.Component;

@Component
public class About implements Function<Void, String> {

    @Override
    public String apply(Void unused) {
        return new JSONStringer().object().key("status").value("healthy").key("version").value("1.0.0").endObject().toString();
    }
}
