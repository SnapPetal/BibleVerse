package com.thonbecker.bibleverse.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class AboutResponse {
  private String status;
  private String packageName;
  private String version;
  private Instant time;
}
