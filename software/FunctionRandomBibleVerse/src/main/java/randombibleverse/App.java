package randombibleverse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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
            String output = this.getAsString(booksInputStream);

            return response.withStatusCode(200).withBody(output);
        } catch (IOException e) {
            return response.withBody("{}").withStatusCode(500);
        }
    }

    @Tracing(namespace = "getFile")
    private InputStream getFile(String fileName) throws SdkClientException {
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
}