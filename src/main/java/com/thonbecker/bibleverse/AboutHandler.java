package com.thonbecker.bibleverse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thonbecker.bibleverse.model.AboutResponse;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AboutHandler implements Supplier<String> {
  @Autowired BuildProperties buildProperties;

  @Override
  public String get() {
    AboutResponse aboutResponse =
        AboutResponse.builder()
            .status("healthy")
            .packageName(buildProperties.getName())
            .version(buildProperties.getVersion())
            .time(buildProperties.getTime().toString())
            .build();

    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(aboutResponse);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      return null;
    }
  }
}
