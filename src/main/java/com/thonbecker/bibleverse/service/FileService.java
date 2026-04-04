package com.thonbecker.bibleverse.service;

import java.io.InputStream;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class FileService {
    private final S3Client s3Client;
    private final String bucketName;

    public FileService(BibleVerseProperties properties) {
        this.bucketName = properties.bucketName();
        this.s3Client = S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public InputStream getFile(String fileName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .key(fileName)
                .bucket(bucketName)
                .build();
        try {
            return this.s3Client.getObject(objectRequest);
        } catch (SdkException e) {
            throw new IllegalStateException("Failed to load S3 object: " + fileName, e);
        }
    }
}
