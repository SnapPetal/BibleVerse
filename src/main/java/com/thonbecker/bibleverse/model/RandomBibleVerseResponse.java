package com.thonbecker.bibleverse.model;

import java.util.Map;
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
    private Map<String, String> text;
}
