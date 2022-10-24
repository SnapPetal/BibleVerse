package com.thonbecker.bibleverse.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.stereotype.Service;

@Service
public class FileService {
  private final AmazonS3 s3client =
      AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

  public InputStream getFile(String fileName) {
    return s3client.getObject("bible-verse-data-files", fileName).getObjectContent();
  }

  public String getFileAsString(InputStream is) throws IOException {
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
}
