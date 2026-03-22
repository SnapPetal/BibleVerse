package com.thonbecker.bibleverse.functions;

import com.thonbecker.bibleverse.model.RandomBibleVerseResponse;
import com.thonbecker.bibleverse.service.FileService;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
@Slf4j
public class RandomBibleVerseHandler implements Supplier<RandomBibleVerseResponse> {
    private final Document csbDoc;
    private final Document greekDoc;

    private static final List<String> BOOK_NAMES = List.of(
            "Genesis",
            "Exodus",
            "Leviticus",
            "Numbers",
            "Deuteronomy",
            "Joshua",
            "Judges",
            "Ruth",
            "1 Samuel",
            "2 Samuel",
            "1 Kings",
            "2 Kings",
            "1 Chronicles",
            "2 Chronicles",
            "Ezra",
            "Nehemiah",
            "Esther",
            "Job",
            "Psalms",
            "Proverbs",
            "Ecclesiastes",
            "Song of Solomon",
            "Isaiah",
            "Jeremiah",
            "Lamentations",
            "Ezekiel",
            "Daniel",
            "Hosea",
            "Joel",
            "Amos",
            "Obadiah",
            "Jonah",
            "Micah",
            "Nahum",
            "Habakkuk",
            "Zephaniah",
            "Haggai",
            "Zechariah",
            "Malachi",
            "Matthew",
            "Mark",
            "Luke",
            "John",
            "Acts",
            "Romans",
            "1 Corinthians",
            "2 Corinthians",
            "Galatians",
            "Ephesians",
            "Philippians",
            "Colossians",
            "1 Thessalonians",
            "2 Thessalonians",
            "1 Timothy",
            "2 Timothy",
            "Titus",
            "Philemon",
            "Hebrews",
            "James",
            "1 Peter",
            "2 Peter",
            "1 John",
            "2 John",
            "3 John",
            "Jude",
            "Revelation");

    public RandomBibleVerseHandler(FileService fileService) {
        try {
            this.csbDoc = parseXml(fileService.getFile("csb/bible.xml"));
            this.greekDoc = parseXml(fileService.getFile("greek/bible.xml"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Bible XML data", e);
        }
    }

    @Override
    public RandomBibleVerseResponse get() {
        return getRandomVerse();
    }

    private Document parseXml(InputStream is) throws Exception {
        try (is) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            return factory.newDocumentBuilder().parse(is);
        }
    }

    private RandomBibleVerseResponse getRandomVerse() {
        Map<String, String> verseText = new HashMap<>();

        // Get all book elements from CSB
        NodeList csbBooks = csbDoc.getElementsByTagName("book");
        int randomBookIndex = getRandomNumber(csbBooks.getLength()) - 1;
        Element csbBook = (Element) csbBooks.item(randomBookIndex);
        int bookNumber = Integer.parseInt(csbBook.getAttribute("number"));
        String bookName = BOOK_NAMES.get(bookNumber - 1);

        // Get random chapter
        NodeList chapters = csbBook.getElementsByTagName("chapter");
        int randomChapterIndex = getRandomNumber(chapters.getLength()) - 1;
        Element chapter = (Element) chapters.item(randomChapterIndex);
        String chapterNumber = chapter.getAttribute("number");

        // Get random verse
        NodeList verses = chapter.getElementsByTagName("verse");
        int randomVerseIndex = getRandomNumber(verses.getLength()) - 1;
        Element verse = (Element) verses.item(randomVerseIndex);
        String verseNumber = verse.getAttribute("number");
        verseText.put("CSB", verse.getTextContent());

        // Look up Greek text
        NodeList greekBooks = greekDoc.getElementsByTagName("book");
        for (int i = 0; i < greekBooks.getLength(); i++) {
            Element greekBook = (Element) greekBooks.item(i);
            if (greekBook.getAttribute("number").equals(String.valueOf(bookNumber))) {
                NodeList greekChapters = greekBook.getElementsByTagName("chapter");
                for (int j = 0; j < greekChapters.getLength(); j++) {
                    Element greekChapter = (Element) greekChapters.item(j);
                    if (greekChapter.getAttribute("number").equals(chapterNumber)) {
                        NodeList greekVerses = greekChapter.getElementsByTagName("verse");
                        for (int k = 0; k < greekVerses.getLength(); k++) {
                            Element greekVerse = (Element) greekVerses.item(k);
                            if (greekVerse.getAttribute("number").equals(verseNumber)) {
                                verseText.put("Greek", greekVerse.getTextContent());
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }

        if (!verseText.containsKey("Greek")) {
            log.info("No Greek data found for {} {}:{}", bookName, chapterNumber, verseNumber);
        }

        return RandomBibleVerseResponse.builder()
                .book(bookName)
                .chapter(chapterNumber)
                .verse(verseNumber)
                .text(verseText)
                .build();
    }

    private int getRandomNumber(int max) {
        return new Random().ints(1, max).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
