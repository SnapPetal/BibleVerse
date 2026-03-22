package com.thonbecker.bibleverse.functions;

import com.thonbecker.bibleverse.model.RandomBibleVerseResponse;
import com.thonbecker.bibleverse.service.FileService;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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
    private final List<Book> books;

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
            Document csbDoc = parseXml(fileService.getFile("csb/bible.xml"));
            Document greekDoc = parseXml(fileService.getFile("greek/bible.xml"));
            this.books = indexBibles(csbDoc, greekDoc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Bible XML data", e);
        }
    }

    @Override
    public RandomBibleVerseResponse get() {
        Book book = books.get(ThreadLocalRandom.current().nextInt(books.size()));
        Chapter chapter = book.chapters.get(ThreadLocalRandom.current().nextInt(book.chapters.size()));
        Verse verse = chapter.verses.get(ThreadLocalRandom.current().nextInt(chapter.verses.size()));

        Map<String, String> verseText = new HashMap<>();
        verseText.put("CSB", verse.csb);
        if (verse.greek != null) {
            verseText.put("Greek", verse.greek);
        } else {
            log.info("No Greek data found for {} {}:{}", book.name, chapter.number, verse.number);
        }

        return RandomBibleVerseResponse.builder()
                .book(book.name)
                .chapter(chapter.number)
                .verse(verse.number)
                .text(verseText)
                .build();
    }

    private Document parseXml(InputStream is) throws Exception {
        try (is) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            return factory.newDocumentBuilder().parse(is);
        }
    }

    private List<Book> indexBibles(Document csbDoc, Document greekDoc) {
        // Index Greek text by book/chapter/verse number for O(1) lookup
        Map<String, Map<String, Map<String, String>>> greekIndex = new HashMap<>();
        NodeList greekBooks = greekDoc.getElementsByTagName("book");
        for (int i = 0; i < greekBooks.getLength(); i++) {
            Element greekBook = (Element) greekBooks.item(i);
            String bookNum = greekBook.getAttribute("number");
            Map<String, Map<String, String>> chapterMap = new HashMap<>();
            greekIndex.put(bookNum, chapterMap);
            NodeList greekChapters = greekBook.getElementsByTagName("chapter");
            for (int j = 0; j < greekChapters.getLength(); j++) {
                Element greekChapter = (Element) greekChapters.item(j);
                String chapterNum = greekChapter.getAttribute("number");
                Map<String, String> verseMap = new HashMap<>();
                chapterMap.put(chapterNum, verseMap);
                NodeList greekVerses = greekChapter.getElementsByTagName("verse");
                for (int k = 0; k < greekVerses.getLength(); k++) {
                    Element greekVerse = (Element) greekVerses.item(k);
                    verseMap.put(greekVerse.getAttribute("number"), greekVerse.getTextContent());
                }
            }
        }

        // Build indexed book list from CSB, merging Greek text
        List<Book> result = new ArrayList<>();
        NodeList csbBooks = csbDoc.getElementsByTagName("book");
        for (int i = 0; i < csbBooks.getLength(); i++) {
            Element csbBook = (Element) csbBooks.item(i);
            String bookNum = csbBook.getAttribute("number");
            String bookName = BOOK_NAMES.get(Integer.parseInt(bookNum) - 1);
            Map<String, Map<String, String>> greekChapters = greekIndex.get(bookNum);

            List<Chapter> chapters = new ArrayList<>();
            NodeList csbChapters = csbBook.getElementsByTagName("chapter");
            for (int j = 0; j < csbChapters.getLength(); j++) {
                Element csbChapter = (Element) csbChapters.item(j);
                String chapterNum = csbChapter.getAttribute("number");
                Map<String, String> greekVerses = greekChapters != null ? greekChapters.get(chapterNum) : null;

                List<Verse> verses = new ArrayList<>();
                NodeList csbVerses = csbChapter.getElementsByTagName("verse");
                for (int k = 0; k < csbVerses.getLength(); k++) {
                    Element csbVerse = (Element) csbVerses.item(k);
                    String verseNum = csbVerse.getAttribute("number");
                    String greekText = greekVerses != null ? greekVerses.get(verseNum) : null;
                    verses.add(new Verse(verseNum, csbVerse.getTextContent(), greekText));
                }
                chapters.add(new Chapter(chapterNum, List.copyOf(verses)));
            }
            result.add(new Book(bookName, List.copyOf(chapters)));
        }
        return List.copyOf(result);
    }

    private record Verse(String number, String csb, String greek) {}

    private record Chapter(String number, List<Verse> verses) {}

    private record Book(String name, List<Chapter> chapters) {}
}
