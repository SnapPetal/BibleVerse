package com.myorg;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BibleVerseTest {
    private final static ObjectMapper JSON = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

    @Test
    public void testStack() throws IOException {
        App app = new App();
        BibleVerseStack stack = new BibleVerseStack(app, "Test", StackProps.builder()
                // Uncomment the next block to specialize this stack for the AWS Account
                // and Region that are implied by the current CLI configuration.
                .env(Environment.builder().account("345435345").region("us-east-1").build()).build());

        // synthesize the stack to a CloudFormation template
        JsonNode actual = JSON.valueToTree(app.synth().getStackArtifact(stack.getArtifactId()).getTemplate());

        // Update once resources have been added to the stack
        assertThat(actual.get("Resources")).isNotNull();
    }
}
