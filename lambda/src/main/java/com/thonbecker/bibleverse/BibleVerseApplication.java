package com.thonbecker.bibleverse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

@SpringBootApplication
public class BibleVerseApplication {
    private static final Log logger = LogFactory.getLog(BibleVerseApplication.class);

    public static void main(String[] args) {
        logger.info("==> Starting: BibleVerseApplication");
        if (!ObjectUtils.isEmpty(args)) {
            logger.info("==>  args: " + Arrays.asList(args));
        }
        SpringApplication.run(BibleVerseApplication.class, args);
    }
}
