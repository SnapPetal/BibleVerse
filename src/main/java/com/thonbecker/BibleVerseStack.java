package com.thonbecker;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
import software.amazon.awscdk.services.apigatewayv2.CorsPreflightOptions;
import software.amazon.awscdk.services.apigatewayv2.DomainMappingOptions;
import software.amazon.awscdk.services.apigatewayv2.DomainName;
import software.amazon.awscdk.services.apigatewayv2.DomainNameProps;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion;
import software.amazon.awscdk.services.apigatewayv2.integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.integrations.HttpLambdaIntegrationProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneProviderProps;
import software.amazon.awscdk.services.route53.ARecord;
import software.amazon.awscdk.services.route53.ARecordProps;
import software.amazon.awscdk.services.route53.RecordTarget;
import software.amazon.awscdk.services.route53.targets.ApiGatewayv2DomainProperties;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.core.BundlingOutput.ARCHIVED;

public class BibleVerseStack extends Stack {

    public BibleVerseStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket bucket = new Bucket(this, "Bucket", new BucketProps.Builder()
                .removalPolicy(RemovalPolicy.DESTROY).autoDeleteObjects(true).build());

        List<ISource> sources = new ArrayList<>(1);
        sources.add(Source.asset("./data"));

        BucketDeployment bucketDeployment = new BucketDeployment(this, "BucketDeployment",
                new BucketDeploymentProps.Builder().sources(sources).destinationBucket(bucket).build());

        List<String> BibleVersePackagingInstructions = Arrays.asList("/bin/sh", "-c",
                "mvn clean install " + "&& cp /asset-input/target/bibleverse.jar /asset-output/");

        BundlingOptions.Builder builderOptions = BundlingOptions.builder()
                .command(BibleVersePackagingInstructions).image(Runtime.JAVA_11.getBundlingImage())
                .volumes(singletonList(DockerVolume.builder()
                        .hostPath(System.getProperty("user.home") + "/.m2/")
                        .containerPath("/root/.m2/").build()))
                .user("root").outputType(ARCHIVED);

        Map<String, String> environmentMap = new HashMap<>();
        environmentMap.put("DATA_BUCKET_NAME", bucket.getBucketName());

        Function functionRandomBibleVerse = new Function(this, "FunctionRandomBibleVerse", FunctionProps
                .builder().runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("./lambda/",
                        AssetOptions.builder().bundling(builderOptions.build()).build()))
                .handler("com.thonbecker.randombibleverse.LambdaHandler").memorySize(1024)
                .timeout(Duration.seconds(10)).logRetention(RetentionDays.ONE_WEEK)
                .environment(environmentMap).build());

        Function functionAboutBibleVerse = new Function(this, "FunctionAboutBibleVerse", FunctionProps.builder()
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("./lambda/",
                        AssetOptions.builder().bundling(builderOptions.build()).build()))
                .handler("com.thonbecker.about.LambdaHandler").memorySize(1024)
                .timeout(Duration.seconds(10)).logRetention(RetentionDays.ONE_WEEK)
                .environment(environmentMap).build());

        bucket.grantRead(functionRandomBibleVerse);

        DomainName domainName = new DomainName(this, "DomainName", DomainNameProps.builder()
                .domainName("bibleverse.thonbecker.com")
                .certificate(Certificate.fromCertificateArn(this, "CertificateLookup",
                        "arn:aws:acm:us-east-1:664759038511:certificate/1554896f-abd0-4a6b-8955-710f2f4a642b"))
                .build());

        List<String> corsOrigins = new ArrayList<>(1);
        corsOrigins.add("*");

        List<CorsHttpMethod> corsHttpMethods = new ArrayList<>(2);
        corsHttpMethods.add(CorsHttpMethod.GET);
        corsHttpMethods.add(CorsHttpMethod.OPTIONS);

        List<HttpMethod> httpMethods = new ArrayList<>();
        httpMethods.add(HttpMethod.GET);

        HttpApi httpApi = new HttpApi(this, "bibleverse-api", HttpApiProps.builder().apiName("bibleverse-api")
                .createDefaultStage(true)
                .corsPreflight(CorsPreflightOptions.builder().allowMethods(corsHttpMethods)
                        .allowOrigins(corsOrigins).build())
                .defaultDomainMapping(DomainMappingOptions.builder().domainName(domainName).build())
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder().path("/about").methods(httpMethods)
                .integration(new HttpLambdaIntegration("LambdaIntegrationAbout", functionAboutBibleVerse,
                HttpLambdaIntegrationProps.builder()
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0).build()))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder().path("/random").methods(httpMethods)
                .integration(new HttpLambdaIntegration("LambdaIntegrationRandom",functionRandomBibleVerse,
                HttpLambdaIntegrationProps.builder()
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0).build()))
                .build());

        ARecord aliasRecord = new ARecord(this, "AliasRecord", ARecordProps.builder()
                .recordName("bibleverse.thonbecker.com")
                .zone(HostedZone.fromLookup(this, "HostedZoneLookup",
                        HostedZoneProviderProps.builder().domainName("thonbecker.com").build()))
                .target(RecordTarget.fromAlias(
                        new ApiGatewayv2DomainProperties(domainName.getRegionalDomainName(),
                                domainName.getRegionalHostedZoneId())))
                .build());
    }
}