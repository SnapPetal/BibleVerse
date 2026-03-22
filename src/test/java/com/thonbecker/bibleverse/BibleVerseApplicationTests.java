package com.thonbecker.bibleverse;

import static org.junit.jupiter.api.Assertions.*;

import com.thonbecker.bibleverse.functions.RandomBibleVerseHandler;
import com.thonbecker.bibleverse.model.RandomBibleVerseResponse;
import com.thonbecker.bibleverse.service.FileService;
import java.io.FileInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BibleVerseApplicationTests {

    @Test
    void randomBibleVerseHandler_returnsValidVerse() throws Exception {
        FileService fileService = Mockito.mock(FileService.class);
        Mockito.when(fileService.getFile("csb/bible.xml"))
                .thenReturn(new FileInputStream(".infrastructure/lib/data/files/csb/bible.xml"));
        Mockito.when(fileService.getFile("greek/bible.xml"))
                .thenReturn(new FileInputStream(".infrastructure/lib/data/files/greek/bible.xml"));

        RandomBibleVerseHandler handler = new RandomBibleVerseHandler(fileService);
        RandomBibleVerseResponse response = handler.get();

        assertNotNull(response.getBook());
        assertNotNull(response.getChapter());
        assertNotNull(response.getVerse());
        assertNotNull(response.getText());
        assertTrue(response.getText().containsKey("CSB"));
        assertTrue(response.getText().containsKey("Greek"));
        assertFalse(response.getText().get("CSB").isEmpty());
        assertFalse(response.getText().get("Greek").isEmpty());
    }
}
