import { Stack, StackProps, RemovalPolicy, } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as s3deploy from 'aws-cdk-lib/aws-s3-deployment';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as route53 from 'aws-cdk-lib/aws-route53';
import * as targets from 'aws-cdk-lib/aws-route53-targets';
import {
  HttpApi,
  DomainName,
  HttpMethod,
  CorsHttpMethod,
} from '@aws-cdk/aws-apigatewayv2-alpha';
import {
  HttpLambdaIntegration,
} from '@aws-cdk/aws-apigatewayv2-integrations-alpha';
import * as path from "path";
import { RetentionDays } from 'aws-cdk-lib/aws-logs';

export class BibleVerseStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    const certArn = 'arn:aws:acm:us-east-1:664759038511:certificate/1554896f-abd0-4a6b-8955-710f2f4a642b';
    const domainName = 'bibleverse.thonbecker.com';

    const aboutFunction = new lambda.Function(this, 'AboutFunction', {
      runtime: lambda.Runtime.JAVA_11,
      memorySize: 1024,
      logRetention: RetentionDays.THREE_DAYS,
      handler: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker',
      code: lambda.Code.fromAsset(path.join(__dirname, 'data', 'lambda', 'bibleverse-1.0.0-aws.jar')),
      environment: {
        'SPRING_CLOUD_FUNCTION_DEFINITION': 'aboutHandler'
      },
    });

    const randomBibleVerseFunction = new lambda.Function(this, 'RandomBibleVerseFunction', {
      runtime: lambda.Runtime.JAVA_11,
      memorySize: 1024,
      logRetention: RetentionDays.THREE_DAYS,
      handler: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker',
      code: lambda.Code.fromAsset(path.join(__dirname, 'data', 'lambda', 'bibleverse-1.0.0-aws.jar')),
      environment: {
        'SPRING_CLOUD_FUNCTION_DEFINITION': 'randomBibleVerseHandler'
      },
    });

    const bucket = new s3.Bucket(this, 'BibleVerseBucket', {
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      objectOwnership: s3.ObjectOwnership.BUCKET_OWNER_ENFORCED,
      removalPolicy: RemovalPolicy.DESTROY,
    });

    const deployment = new s3deploy.BucketDeployment(this, 'DeployBibleVerse', {
      sources: [s3deploy.Source.asset(path.join(__dirname, 'data', 'files'))],
      destinationBucket: bucket,
    });

    deployment.deployedBucket.grantRead(randomBibleVerseFunction);
    
    const dn = new DomainName(this, 'DomainNameBibleVerse', {
      domainName,
      certificate: acm.Certificate.fromCertificateArn(this, 'CertificateBibleVerse', certArn),
    });

    const api = new HttpApi(this, 'HttpApiBibleVerse', {
      corsPreflight: {
        allowOrigins: ['https://thonbecker.com', 'https://www.thonbecker.com'],
        allowMethods: [CorsHttpMethod.GET, CorsHttpMethod.OPTIONS],
        allowHeaders: [
          'Content-Type',
          'Access-Control-Allow-Headers',
          'Access-Control-Allow-Origin',
          'Access-Control-Allow-Methods',
        ],
      },
      defaultIntegration: new HttpLambdaIntegration('DefaultIntegration', randomBibleVerseFunction),
      defaultDomainMapping: {
        domainName: dn,
      },
    });

    api.addRoutes({
      path: '/about',
      methods: [HttpMethod.GET],
      integration: new HttpLambdaIntegration('AboutIntegration', aboutFunction)
    });

    new route53.ARecord(this, 'AliasBibleVerse', {
      zone: route53.HostedZone.fromHostedZoneAttributes(this, 'HostedZoneBibleVerse', {
        hostedZoneId: 'Z0960080GF0UBO75OWWP',
        zoneName: 'thonbecker.com'
      }),
      recordName: 'bibleverse.thonbecker.com',
      target: route53.RecordTarget.fromAlias(new targets.ApiGatewayv2DomainProperties(dn.regionalDomainName, dn.regionalHostedZoneId)),
    });
  }
}
