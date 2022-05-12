package com.thonbecker.bibleverse;


import com.amazonaws.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.internal.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Random;

@SpringBootApplication
public class BibleVerseApplication {
    private final Random random = new Random();
    private final ResourceLoader resourceLoader;
    private final String dataBucketName = System.getenv("DATA_BUCKET_NAME");

    public BibleVerseApplication(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static void main(String[] args) {
        SpringApplication.run(BibleVerseApplication.class, args);
    }

    @Bean
    public Function<String, String> getAbout() {
        return value -> new JSONStringer().object().key("status").value("healthy").key("version").value("1.0.0").endObject().toString();
    }

    @Bean
    public Function<String, String> getRandomBibleVerse() {
        return value -> {
            try {
                InputStream booksInputStream = this.getFile("Books.json");
                String booksData = this.getAsString(booksInputStream);

                String book = this.getRandomBook(booksData);
                InputStream bookInputStream = this.getFile(book);
                String bookData = this.getAsString(bookInputStream);

                return this.getRandomVerse(bookData);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        };
    }

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
        return new JSONStringer().object().key("book").value(bookObject.getString("book")).key("chapter").value(randomChapterObject.getString("chapter")).key("verse").value(randomVerseObject.getString("verse")).key("text").value(randomVerseObject.getString("text")).endObject().toString();
    }

    private String getRandomBook(String booksData) {
        JSONObject booksObject = new JSONObject(booksData);
        JSONArray filesArray = booksObject.getJSONArray("files");
        int randomIndex = this.getRandomNumber(filesArray.length());
        return filesArray.getString(randomIndex - 1);
    }

    private InputStream getFile(String fileName) throws IOException {
        String s3Url = String.valueOf(Paths.get(dataBucketName, fileName));
        Resource resource = resourceLoader.getResource(s3Url);
        return resource.getInputStream();
    }

    private String getAsString(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StringUtils.UTF8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private int getRandomNumber(int max) {
        return random.ints(1, max).findFirst().getAsInt();
    }
}
