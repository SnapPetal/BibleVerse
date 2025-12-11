package com.thonbecker.bibleverse.functions;

import com.thonbecker.bibleverse.model.BookData;
import com.thonbecker.bibleverse.model.RandomBibleVerseResponse;
import com.thonbecker.bibleverse.service.FileService;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RandomBibleVerseHandler implements Supplier<RandomBibleVerseResponse> {
    private final FileService fileService;

    @Override
    public RandomBibleVerseResponse get() {
        try {
            String booksFileData = fileService
                    .getFileAsString(fileService.getFile("kjv/Books.json"))
                    .orElseThrow();
            String lemmaFileData = fileService
                    .getFileAsString(fileService.getFile("lemma/bible.json"))
                    .orElseThrow();
            BookData bookData = this.getRandomBook(booksFileData);
            String bookFileData = fileService
                    .getFileAsString(fileService.getFile(String.format("kjv/%s", bookData.getFileName())))
                    .orElseThrow();

            return this.getRandomVerse(bookData, bookFileData, lemmaFileData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RandomBibleVerseResponse getRandomVerse(BookData bookData, String bookFileData, String lemmaFileData) {
        JSONObject bookObject = new JSONObject(bookFileData);
        JSONObject lemmaObject = new JSONObject(lemmaFileData);
        Map<String, String> verseText = new HashMap<>();

        // Look up random chapter from the book
        JSONArray chaptersArray = bookObject.getJSONArray("chapters");
        JSONObject randomChapterObject = chaptersArray.getJSONObject(getRandomNumber(chaptersArray.length()) - 1);

        // Look up random verse from the chapter
        JSONArray randomVerseArray = randomChapterObject.getJSONArray("verses");
        JSONObject randomVerseObject = randomVerseArray.getJSONObject(getRandomNumber(randomVerseArray.length()) - 1);
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

        return RandomBibleVerseResponse.builder()
                .book(bookData.getName())
                .chapter(randomChapterObject.getString("chapter"))
                .verse(randomVerseObject.getString("verse"))
                .text(verseText)
                .build();
    }

    private BookData getRandomBook(String booksData) {
        JSONObject booksObject = new JSONObject(booksData);
        JSONArray bookNameArray = booksObject.getJSONArray("names");
        JSONArray filesArray = booksObject.getJSONArray("files");
        int randomIndex = getRandomNumber(filesArray.length());
        log.info("Random index is: {}", randomIndex);
        return new BookData(bookNameArray.getString(randomIndex - 1), filesArray.getString(randomIndex - 1));
    }

    private int getRandomNumber(int max) {
        return new Random().ints(1, max).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
