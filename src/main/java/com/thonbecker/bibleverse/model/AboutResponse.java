package com.thonbecker.bibleverse.model;

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
  private String time;
}
