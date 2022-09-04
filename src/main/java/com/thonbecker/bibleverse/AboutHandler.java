package com.thonbecker.bibleverse;

import java.util.function.Supplier;
import org.json.JSONStringer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class AboutHandler implements Supplier<String> {
  @Autowired BuildProperties buildProperties;

  @Override
  public String get() {
    return new JSONStringer()
        .object()
        .key("status")
        .value("healthy")
        .key("package")
        .value(buildProperties.getName())
        .key("version")
        .value(buildProperties.getVersion())
        .key("time")
        .value(buildProperties.getTime())
        .endObject()
        .toString();
  }
}
