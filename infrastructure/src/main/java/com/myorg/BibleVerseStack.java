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
import software.amazon.awscdk.services.apigatewayv2.DomainName;
import software.amazon.awscdk.services.apigatewayv2.DomainNameProps;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegrationProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneAttributes;
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

        public BibleVerseStack(final Construct scope, final String id) {
                this(scope, id, null);
        }

        public BibleVerseStack(final Construct scope, final String id, final StackProps props) {
                super(scope, id, props);

                Bucket bucket = new Bucket(this, "Bucket", new BucketProps.Builder()
                                .removalPolicy(RemovalPolicy.DESTROY).autoDeleteObjects(true).build());

                List<ISource> sources = new ArrayList<>(1);
                sources.add(Source.asset("../data"));

                BucketDeployment bucketDeployment = new BucketDeployment(this, "BucketDeployment",
                                new BucketDeploymentProps.Builder().sources(sources).destinationBucket(bucket).build());

                List<String> functionRandomBibleVersePackagingInstructions = Arrays.asList("/bin/sh", "-c",
                                "cd FunctionRandomBibleVerse " + "&& mvn clean install "
                                                + "&& cp /asset-input/FunctionRandomBibleVerse/target/functionrandombibleverse.jar /asset-output/");

                BundlingOptions.Builder builderOptions = BundlingOptions.builder()
                                .command(functionRandomBibleVersePackagingInstructions)
                                .image(Runtime.JAVA_11.getBundlingImage())
                                .volumes(singletonList(DockerVolume.builder()
                                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                                .containerPath("/root/.m2/").build()))
                                .user("root").outputType(ARCHIVED);

                Function functionRandomBibleVerse = new Function(this, "FunctionRandomBibleVerse",
                                FunctionProps.builder().runtime(Runtime.JAVA_11).code(Code.fromAsset("../software/",
                                                AssetOptions.builder().bundling(builderOptions
                                                                .command(functionRandomBibleVersePackagingInstructions)
                                                                .build()).build()))
                                                .handler("randombibleverse.App").memorySize(1024)
                                                .timeout(Duration.seconds(10)).logRetention(RetentionDays.ONE_WEEK)
                                                .build());

                DomainName domainName = new DomainName(this, "DomainName", DomainNameProps.builder()
                                .domainName("bibleverse.thonbecker.com")
                                .certificate(Certificate.fromCertificateArn(this, "CertificateLookup",
                                                "arn:aws:acm:us-west-2:664759038511:certificate/6bb68629-cffb-40a2-8ba4-dcc2d57259c4"))
                                .build());

                HttpApi httpApi = new HttpApi(this, "bibleverse-api", HttpApiProps.builder().apiName("bibleverse-api")
                                .createDefaultStage(true)
                                .defaultDomainMapping(DomainMappingOptions.builder().domainName(domainName).build())
                                .defaultIntegration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
                                                .handler(functionRandomBibleVerse)
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
