package com.thonbecker.bibleverse.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Service
public class FileService {
  private final S3Client s3Client =
      S3Client.builder()
          .region(Region.US_EAST_1)
          .credentialsProvider(DefaultCredentialsProvider.create())
          .build();

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
