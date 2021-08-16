package com.myorg;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awscdk.core.BundlingOptions;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.DockerVolume;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.BucketDeploymentProps;
import software.amazon.awscdk.services.s3.deployment.ISource;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.amazon.awscdk.services.apigatewayv2.DomainMappingOptions;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegrationProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import static java.util.Collections.singletonList;
import static software.amazon.awscdk.core.BundlingOutput.ARCHIVED;

public class BibleVerseStack extends Stack {

    public BibleVerseStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public BibleVerseStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket bucket = new Bucket(this, "Bucket",
                new BucketProps.Builder().removalPolicy(RemovalPolicy.DESTROY).autoDeleteObjects(true).build());

        List<ISource> sources = new ArrayList<>(1);
        sources.add(Source.asset("../data"));

        BucketDeployment bucketDeployment = new BucketDeployment(this, "BucketDeployment",
                new BucketDeploymentProps.Builder().sources(sources).destinationBucket(bucket).build());

        List<String> functionRandomBibleVersePackagingInstructions = Arrays.asList("/bin/sh", "-c", "cd FunctionRandomBibleVerse "
                + "&& mvn clean install "
                + "&& cp /asset-input/FunctionRandomBibleVerse/target/functionrandombibleverse.jar /asset-output/");

        BundlingOptions.Builder builderOptions = BundlingOptions.builder()
                .command(functionRandomBibleVersePackagingInstructions)
                .image(Runtime.JAVA_11.getBundlingImage())
                .volumes(singletonList(
                        DockerVolume.builder()
                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                .containerPath("/root/.m2/")
                                .build()
                ))
                .user("root")
                .outputType(ARCHIVED);

        Function functionRandomBibleVerse = new Function(this, "FunctionRandomBibleVerse", FunctionProps.builder()
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../software/",
                        AssetOptions.builder()
                                .bundling(builderOptions.command(functionRandomBibleVersePackagingInstructions).build())
                                .build()))
                .handler("randombibleverse.App").memorySize(1024).timeout(Duration.seconds(10))
                .logRetention(RetentionDays.ONE_WEEK).build());


        HttpApi httpApi = new HttpApi(this, "bibleverse-api", HttpApiProps.builder()
        .apiName("bibleverse-api")
        .createDefaultStage(true)
        .defaultIntegration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
        .handler(functionRandomBibleVerse).payloadFormatVersion(PayloadFormatVersion.VERSION_2_0).build())).build());
    }
}
