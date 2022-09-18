package com.thonbecker.bibleverse;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.*;
import java.util.Random;
import java.util.function.Supplier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.stereotype.Component;

@Component
public class RandomBibleVerseHandler implements Supplier<String> {
  @Override
  public String get() {
    try {
      InputStream booksInputStream = this.getFile("kjv/Books.json");
      String booksData = this.getAsString(booksInputStream);

      String book = this.getRandomBook(booksData);
      InputStream bookInputStream = this.getFile(String.format("kjv/%s", book));
      String bookData = this.getAsString(bookInputStream);

      return this.getRandomVerse(bookData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private AmazonS3 s3client =
      AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

  private String getRandomVerse(String bookData) {
    JSONObject bookObject = new JSONObject(bookData);

    // Lookup random chapter from the book
    JSONArray chaptersArray = bookObject.getJSONArray("chapters");
    int randomChapterIndex = this.getRandomNumber(chaptersArray.length());
    JSONObject randomChapterObject = chaptersArray.getJSONObject(randomChapterIndex - 1);

    // Lookup random verse from the chapter
    JSONArray randomVerseArray = randomChapterObject.getJSONArray("verses");
    int randomVerseIndex = this.getRandomNumber(randomVerseArray.length());
    JSONObject randomVerseObject = randomVerseArray.getJSONObject(randomVerseIndex - 1);

    // Create JSON object
    return new JSONStringer()
        .object()
        .key("book")
        .value(bookObject.getString("book"))
        .key("chapter")
        .value(randomChapterObject.getString("chapter"))
        .key("verse")
        .value(randomVerseObject.getString("verse"))
        .key("text")
        .value(randomVerseObject.getString("text"))
        .endObject()
        .toString();
  }

  private String getRandomBook(String booksData) {
    JSONObject booksObject = new JSONObject(booksData);
    JSONArray filesArray = booksObject.getJSONArray("files");
    int randomIndex = this.getRandomNumber(filesArray.length());
    return filesArray.getString(randomIndex - 1);
  }

  private InputStream getFile(String fileName) {
    return s3client.getObject("bible-verse-data-files", fileName).getObjectContent();
  }

  private String getAsString(InputStream is) throws IOException {
    if (is == null) return "";
    StringBuilder sb = new StringBuilder();
    try (is) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }
    return sb.toString();
  }

  private int getRandomNumber(int max) {
    Random random = new Random();
    return random.ints(1, max).findFirst().getAsInt();
  }
}
