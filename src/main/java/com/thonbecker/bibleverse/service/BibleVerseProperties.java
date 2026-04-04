package com.thonbecker.bibleverse.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bibleverse.s3")
public record BibleVerseProperties(String bucketName, String region) {}
