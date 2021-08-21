package com.thonbecker.randombibleverse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Random;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
/**
 * Handler for requests to Lambda function.
 */
public class LambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private AmazonS3 s3client;

    public LambdaHandler() {
        s3client = AmazonS3ClientBuilder.defaultClient();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent event, final Context context) {
        Map<String, String> headers = Map.of("Content-Type", "application/json");

        try {
            InputStream booksInputStream = this.getFile("Books.json");
            String booksData = this.getAsString(booksInputStream);

            String book = this.getRandomBook(booksData);
            InputStream bookInputStream = this.getFile(book);
            String bookData = this.getAsString(bookInputStream);

            String output = this.getRandomVerse(bookData);

            return APIGatewayV2HTTPResponse.builder().withStatusCode(200).withBody(output)
                    .withHeaders(headers).build();

        } catch (IOException e) {
            return APIGatewayV2HTTPResponse.builder().withStatusCode(500)
                    .withBody(getErrorMessage(e)).withHeaders(headers).build();
        }
    }

    private String getErrorMessage(Exception exception) {
        return new JSONStringer().object().key("error_message").value(exception.getLocalizedMessage()).endObject()
                .toString();
    }

    private String getRandomVerse(String bookData) throws IOException {
        JSONObject bookObject = new JSONObject(bookData);

        // Lookup random chapter from the book
        JSONArray chaptersArray = bookObject.getJSONArray("chapters");
        int randomChapterIndex = this.getRandomNumber(1, chaptersArray.length());
        JSONObject randomChapterObject = chaptersArray.getJSONObject(randomChapterIndex - 1);

        // Lookup random verse from the chapter
        JSONArray randomVerseArray = randomChapterObject.getJSONArray("verses");
        int randomVerseIndex = this.getRandomNumber(1, randomVerseArray.length());
        JSONObject randomVerseObject = randomVerseArray.getJSONObject(randomVerseIndex - 1);

        // Create JSON object
        return new JSONStringer().object().key("book").value(bookObject.getString("book")).key("chapter")
                .value(randomChapterObject.getString("chapter")).key("verse")
                .value(randomVerseObject.getString("verse")).key("text").value(randomVerseObject.getString("text"))
                .endObject().toString();
    }

    private String getRandomBook(String booksData) throws IOException {
        JSONObject booksObject = new JSONObject(booksData);
        JSONArray filesArray = booksObject.getJSONArray("files");
        int randomIndex = this.getRandomNumber(1, filesArray.length());
        return filesArray.getString(randomIndex - 1);
    }

    private InputStream getFile(String fileName) throws IOException {
        final String dataBucketName = System.getenv("DATA_BUCKET_NAME");

        S3Object s3object = s3client.getObject(dataBucketName, fileName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        return inputStream;
    }

    private String getAsString(InputStream is) throws IOException {
        if (is == null)
            return "";
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StringUtils.UTF8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }

    private int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.ints(min, max).findFirst().getAsInt();
    }
}