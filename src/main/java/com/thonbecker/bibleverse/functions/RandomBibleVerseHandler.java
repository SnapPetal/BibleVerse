package com.thonbecker.bibleverse.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thonbecker.bibleverse.model.BookData;
import com.thonbecker.bibleverse.model.RandomBibleVerseResponse;
import com.thonbecker.bibleverse.service.FileService;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RandomBibleVerseHandler implements Supplier<RandomBibleVerseResponse> {
  @Autowired private FileService fileService;

  @Override
  public RandomBibleVerseResponse get() {
    try {
      InputStream booksInputStream = fileService.getFile("kjv/Books.json");
      String booksFileData = fileService.getFileAsString(booksInputStream);

      InputStream lemmaInputStream = fileService.getFile("lemma/bible.json");
      String lemmaFileData = fileService.getFileAsString(lemmaInputStream);

      BookData bookData = this.getRandomBook(booksFileData);
      InputStream bookInputStream =
          fileService.getFile(String.format("kjv/%s", bookData.getFileName()));
      String bookFileData = fileService.getFileAsString(bookInputStream);

      return this.getRandomVerse(bookData, bookFileData, lemmaFileData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private RandomBibleVerseResponse getRandomVerse(BookData bookData, String bookFileData, String lemmaFileData)
      throws JsonProcessingException {
    JSONObject bookObject = new JSONObject(bookFileData);
    JSONObject lemmaObject = new JSONObject(lemmaFileData);
    Map<String, String> verseText = new HashMap<>();

    // Lookup random chapter from the book
    JSONArray chaptersArray = bookObject.getJSONArray("chapters");
    int randomChapterIndex = this.getRandomNumber(chaptersArray.length());
    JSONObject randomChapterObject = chaptersArray.getJSONObject(randomChapterIndex - 1);

    // Lookup random verse from the chapter
    JSONArray randomVerseArray = randomChapterObject.getJSONArray("verses");
    int randomVerseIndex = this.getRandomNumber(randomVerseArray.length());
    JSONObject randomVerseObject = randomVerseArray.getJSONObject(randomVerseIndex - 1);
    verseText.put("KJV", randomVerseObject.getString("text"));

    // Lookup lemma data
    if (lemmaObject.has(bookData.getName())) {
      verseText.put(
          "SBLGNT",
          lemmaObject
              .getJSONObject(bookData.getName())
              .getJSONObject(randomChapterObject.getString("chapter"))
              .getString(randomVerseObject.getString("verse")));
    } else {
      log.info("No data found for book: {}", bookData.getFileName());
    }

    RandomBibleVerseResponse randomBibleVerseResponse =
        RandomBibleVerseResponse.builder()
            .book(bookData.getName())
            .chapter(randomChapterObject.getString("chapter"))
            .verse(randomVerseObject.getString("verse"))
            .text(verseText)
            .build();

    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(randomBibleVerseResponse);
  }

  private BookData getRandomBook(String booksData) {
    JSONObject booksObject = new JSONObject(booksData);
    JSONArray bookNameArray = booksObject.getJSONArray("names");
    JSONArray filesArray = booksObject.getJSONArray("files");
    int randomIndex = this.getRandomNumber(filesArray.length());
    log.info("Random index is: {}", randomIndex);
    return new BookData(
        bookNameArray.getString(randomIndex - 1), filesArray.getString(randomIndex - 1));
  }

  private int getRandomNumber(int max) {
    Random random = new Random();
    return random.ints(1, max).findFirst().getAsInt();
  }
}
