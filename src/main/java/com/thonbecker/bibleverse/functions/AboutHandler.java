package com.thonbecker.bibleverse.functions;

import com.thonbecker.bibleverse.model.AboutResponse;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AboutHandler implements Supplier<AboutResponse> {
    private final BuildProperties buildProperties;

    public AboutHandler(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public AboutResponse get() {
        return AboutResponse.builder()
                .status("healthy")
                .packageName(buildProperties.getName())
                .version(buildProperties.getVersion())
                .time(buildProperties.getTime().toString())
                .build();
    }
}
