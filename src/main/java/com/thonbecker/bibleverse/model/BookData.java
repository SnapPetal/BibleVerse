package com.thonbecker.bibleverse.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
public class BookData {
  private String name;
  private String fileName;
}
