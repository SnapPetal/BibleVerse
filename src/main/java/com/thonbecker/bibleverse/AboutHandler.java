package com.thonbecker.bibleverse;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.function.Supplier;
import org.json.JSONStringer;
import org.springframework.stereotype.Component;

@Component
public class AboutHandler implements Supplier<APIGatewayProxyResponseEvent> {

  @Override
  public APIGatewayProxyResponseEvent get() {
    APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
    responseEvent.setStatusCode(200);
    responseEvent.setBody(
        new JSONStringer()
            .object()
            .key("status")
            .value("healthy")
            .key("version")
            .value("1.0.0")
            .endObject()
            .toString());
    return responseEvent;
  }
}
