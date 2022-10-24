package com.thonbecker.bibleverse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thonbecker.bibleverse.model.RandomBibleVerseResponse;
import com.thonbecker.bibleverse.service.FileService;
import java.io.*;
import java.util.Random;
import java.util.function.Supplier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RandomBibleVerseHandler implements Supplier<String> {
  @Autowired private FileService fileService;

  @Override
  public String get() {
    try {
      InputStream booksInputStream = fileService.getFile("kjv/Books.json");
      String booksData = fileService.getFileAsString(booksInputStream);

      String book = this.getRandomBook(booksData);
      InputStream bookInputStream = fileService.getFile(String.format("kjv/%s", book));
      String bookData = fileService.getFileAsString(bookInputStream);

      return this.getRandomVerse(bookData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getRandomVerse(String bookData) throws JsonProcessingException {
    JSONObject bookObject = new JSONObject(bookData);

    // Lookup random chapter from the book
    JSONArray chaptersArray = bookObject.getJSONArray("chapters");
    int randomChapterIndex = this.getRandomNumber(chaptersArray.length());
    JSONObject randomChapterObject = chaptersArray.getJSONObject(randomChapterIndex - 1);

    // Lookup random verse from the chapter
    JSONArray randomVerseArray = randomChapterObject.getJSONArray("verses");
    int randomVerseIndex = this.getRandomNumber(randomVerseArray.length());
    JSONObject randomVerseObject = randomVerseArray.getJSONObject(randomVerseIndex - 1);

    RandomBibleVerseResponse randomBibleVerseResponse =
        RandomBibleVerseResponse.builder()
            .book(bookObject.getString("book"))
            .chapter(randomChapterObject.getString("chapter"))
            .verse(randomVerseObject.getString("verse"))
            .text(randomVerseObject.getString("text"))
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
