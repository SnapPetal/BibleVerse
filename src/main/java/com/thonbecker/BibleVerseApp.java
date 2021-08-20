package com.thonbecker;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

public class BibleVerseApp {
    public static void main(final String[] args) {
        App app = new App();

        new BibleVerseStack(app, "bibleverse-stack", StackProps.builder()
                // Uncomment the next block to specialize this stack for the AWS Account
                // and Region that are implied by the current CLI configuration.
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build());

        app.synth();
    }
}
