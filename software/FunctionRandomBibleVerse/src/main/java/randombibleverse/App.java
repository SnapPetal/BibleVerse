package randombibleverse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

import static software.amazon.lambda.powertools.tracing.CaptureMode.DISABLED;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Tracing(captureMode = DISABLED)
    @Metrics(captureColdStart = true)
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);
        try {
            InputStream booksInputStream = this.getFile("Books.json");
            String booksData = this.getAsString(booksInputStream);

            String book = this.getRandomBook(booksData);
            InputStream bookInputStream = this.getFile(book);
            String bookData = this.getAsString(bookInputStream);

            String output = this.getRandomVerse(bookData);

            return response.withStatusCode(200).withBody(output);
        } catch (IOException e) {
            return response.withBody("{}").withStatusCode(500);
        }
    }

    @Tracing(namespace = "getRandomVerse")
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

    @Tracing(namespace = "getRandomBook")
    private String getRandomBook(String booksData) throws IOException {
        JSONObject booksObject = new JSONObject(booksData);
        JSONArray filesArray = booksObject.getJSONArray("files");
        int randomIndex = this.getRandomNumber(1, filesArray.length());
        return filesArray.getString(randomIndex - 1);
    }

    @Tracing(namespace = "getFile")
    private InputStream getFile(String fileName) throws IOException {
        final String dataBucketName = System.getenv("DATA_BUCKET_NAME");

        AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();

        S3Object s3object = s3client.getObject(dataBucketName, fileName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        return inputStream;
    }

    @Tracing(namespace = "getAsString")
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

    @Tracing(namespace = "getRandomNumber")
    private int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.ints(min, max).findFirst().getAsInt();
    }
}