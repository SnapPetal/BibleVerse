package com.thonbecker.bibleverse.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class FileService {

    private final S3Client s3Client = S3Client.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    public InputStream getFile(String fileName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .key(fileName)
                .bucket("bible-verse-data-files")
                .build();
        return this.s3Client.getObject(objectRequest);
    }

    public Optional<String> getFileAsString(InputStream is) throws IOException {
        if (Objects.isNull(is)) {
            return Optional.empty();
        }
        try (is) {
            return Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
