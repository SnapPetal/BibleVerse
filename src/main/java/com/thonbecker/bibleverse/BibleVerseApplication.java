package com.thonbecker.bibleverse;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ObjectUtils;

@Slf4j
@SpringBootApplication
public class BibleVerseApplication {
  public static void main(String[] args) {
    log.info("==> Starting: BibleVerseApplication");
    if (!ObjectUtils.isEmpty(args)) {
      log.info("==>  args: " + Arrays.asList(args));
    }
    SpringApplication.run(BibleVerseApplication.class, args);
  }
}
