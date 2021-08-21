package com.thonbecker.about;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import org.json.JSONStringer;

/**
 * Handler for requests to Lambda function.
 */
public class LambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Map<String, String> headers = Map.of("Content-Type", "application/json");

    @Override
    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent event, final Context context) {
        return APIGatewayV2HTTPResponse.builder().withStatusCode(200).withBody(getStatusMessage()).withHeaders(headers)
                .build();
    }

    private String getStatusMessage() {
        return new JSONStringer().object().key("status").value("healthy").key("version").value("1.0.0").endObject()
                .toString();
    }
}