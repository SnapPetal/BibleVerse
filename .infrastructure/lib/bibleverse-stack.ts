import { Stack, StackProps, RemovalPolicy, } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as s3deploy from 'aws-cdk-lib/aws-s3-deployment';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as efs from 'aws-cdk-lib/aws-efs';
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
  HttpUrlIntegration,
  HttpLambdaIntegration,
} from '@aws-cdk/aws-apigatewayv2-integrations-alpha';
import * as path from "path";

export class BibleVerseStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    
    const certArn = 'arn:aws:acm:us-east-1:664759038511:certificate/1554896f-abd0-4a6b-8955-710f2f4a642b';
    const domainName = 'bibleverse.thonbecker.com';
    
    const bucket = new s3.Bucket(this, 'BibleVerseBucket',{
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      objectOwnership: s3.ObjectOwnership.BUCKET_OWNER_ENFORCED,
      removalPolicy: RemovalPolicy.DESTROY,
    });

    const deployment = new s3deploy.BucketDeployment(this, 'DeployBibleVerse', {
      sources: [s3deploy.Source.asset(path.join(__dirname, 'data','files'))],
      destinationBucket: bucket,
    });

    const vpc = new ec2.Vpc(this, 'VpcBibleVerse', {
      maxAzs: 2
    });
      
    const securityGroupDataSync = new ec2.SecurityGroup(this, 'SecurityGroupDataSync', {
      vpc,
      allowAllOutbound: true,
      description: 'Security group for a DataSync',
    });
    
    securityGroupDataSync.addEgressRule(
      ec2.Peer.ipv4(vpc.vpcCidrBlock),
      ec2.Port.tcp(2049),
      'allow DataSync outbound access from anywhere',
    );
    
    const securityGroupEfs = new ec2.SecurityGroup(this, 'SecurityGroupEfs', {
      vpc,
      allowAllOutbound: true,
      description: 'Security group for a EFS',
    });
    
    securityGroupEfs.addEgressRule(
      ec2.Peer.ipv4(vpc.vpcCidrBlock),
      ec2.Port.tcp(2049),
      'allow DataSync outbound access from anywhere',
    );
    
    securityGroupEfs.addIngressRule(
      ec2.Peer.ipv4(vpc.vpcCidrBlock),
      ec2.Port.tcp(2049),
      'allow DataSync inbound access',
    );
    
    const fs = new efs.FileSystem(this, 'FileSystemBibleVerse', {
      vpc,
      removalPolicy: RemovalPolicy.DESTROY,
      securityGroup: securityGroupEfs
    });
    
    const accessPoint = fs.addAccessPoint('AccessPointBibleVerse',{
      createAcl: {
        ownerGid: '1001',
        ownerUid: '1001',
        permissions: '750'
      },
      path:'/export/lambda',
      posixUser: {
        gid: '1001',
        uid: '1001'
      }
    });

    const aboutFunction = new lambda.Function(this, 'AboutFunction', {
      runtime: lambda.Runtime.JAVA_11,
      handler: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker',
      code: lambda.Code.fromAsset(path.join(__dirname, 'data','lambda','bibleverse-0.0.1-SNAPSHOT-aws.jar')),
      environment: {
        'spring_cloud_function_definition': 'aboutHandler'
      },
    });

    const randomBibleVerseFunction = new lambda.Function(this, 'RandomBibleVerseFunction', {
      runtime: lambda.Runtime.JAVA_11,
      handler: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker',
      code: lambda.Code.fromAsset(path.join(__dirname, 'data','lambda','bibleverse-0.0.1-SNAPSHOT-aws.jar')),
      environment: {
        'spring_cloud_function_definition': 'randomBibleVerseHandler'
      },
      vpc,
      filesystem: lambda.FileSystem.fromEfsAccessPoint(accessPoint, '/mnt/data')
    });
    
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
      methods: [ HttpMethod.GET ],
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
