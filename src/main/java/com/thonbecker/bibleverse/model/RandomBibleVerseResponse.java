package com.thonbecker.bibleverse.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RandomBibleVerseResponse {
  private String book;
  private String chapter;
  private String verse;
  private String text;
  private String lemma;
}
