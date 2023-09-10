package com.thonbecker.bibleverse.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thonbecker.bibleverse.model.RandomBibleVerseResponse;
import com.thonbecker.bibleverse.service.FileService;
import java.io.*;
import java.util.Random;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RandomBibleVerseHandler implements Supplier<String> {
  @Autowired private FileService fileService;

  @Override
  public String get() {
    try {
      InputStream booksInputStream = fileService.getFile("kjv/Books.json");
      String booksData = fileService.getFileAsString(booksInputStream);

      InputStream lemmaInputStream = fileService.getFile("lemma/bible.json");
      String lemmaData = fileService.getFileAsString(lemmaInputStream);

      String book = this.getRandomBook(booksData);
      InputStream bookInputStream = fileService.getFile(String.format("kjv/%s", book));
      String bookData = fileService.getFileAsString(bookInputStream);

      return this.getRandomVerse(bookData, lemmaData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getRandomVerse(String bookData, String lemmaData) throws JsonProcessingException {
    JSONObject bookObject = new JSONObject(bookData);
    JSONObject lemmaObject = new JSONObject(lemmaData);

    // Lookup random chapter from the book
    JSONArray chaptersArray = bookObject.getJSONArray("chapters");
    int randomChapterIndex = this.getRandomNumber(chaptersArray.length());
    JSONObject randomChapterObject = chaptersArray.getJSONObject(randomChapterIndex - 1);

    // Lookup random verse from the chapter
    JSONArray randomVerseArray = randomChapterObject.getJSONArray("verses");
    int randomVerseIndex = this.getRandomNumber(randomVerseArray.length());
    JSONObject randomVerseObject = randomVerseArray.getJSONObject(randomVerseIndex - 1);

    // Lookup lemma data
    String lemmaVerse = null;
    if (lemmaObject.has(bookObject.getString("book"))) {
      JSONArray lemmaBookArray = lemmaObject.getJSONArray(bookObject.getString("book"));
      JSONArray lemmaChapterObject = lemmaBookArray.getJSONArray(randomChapterIndex - 1);
      lemmaVerse = lemmaChapterObject.getString(randomVerseIndex - 1);
    } else {
      log.info("No data found for book: {}", bookObject.getString("book"));
    }

    RandomBibleVerseResponse randomBibleVerseResponse =
        RandomBibleVerseResponse.builder()
            .book(bookObject.getString("book"))
            .chapter(randomChapterObject.getString("chapter"))
            .verse(randomVerseObject.getString("verse"))
            .text(randomVerseObject.getString("text"))
            .lemma(lemmaVerse)
            .build();

    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(randomBibleVerseResponse);
  }

  private String getRandomBook(String booksData) {
    JSONObject booksObject = new JSONObject(booksData);
    JSONArray filesArray = booksObject.getJSONArray("files");
    int randomIndex = this.getRandomNumber(filesArray.length());
    return filesArray.getString(randomIndex - 1);
  }

  private int getRandomNumber(int max) {
    Random random = new Random();
    return random.ints(1, max).findFirst().getAsInt();
  }
}
