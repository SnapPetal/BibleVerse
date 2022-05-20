import { Stack, StackProps, RemovalPolicy } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as s3deploy from 'aws-cdk-lib/aws-s3-deployment'
import * as lambda from 'aws-cdk-lib/aws-lambda'
import * as path from "path";

export class BibleVerseStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    const bucket = new s3.Bucket(this, 'BibleVerseBucket',{
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      objectOwnership: s3.ObjectOwnership.BUCKET_OWNER_ENFORCED,
      removalPolicy: RemovalPolicy.DESTROY,
    });

    const deployment = new s3deploy.BucketDeployment(this, 'DeployBibleVerse', {
      sources: [s3deploy.Source.asset(path.join(__dirname, 'data','files'))],
      destinationBucket: bucket,
    });

    const aboutFunction = new lambda.Function(this, 'AboutFunction', {
      runtime: lambda.Runtime.PROVIDED,
      handler: 'functionRouter',
      code: lambda.Code.fromAsset(path.join(__dirname, 'data','lambda','bibleverse-0.0.1-SNAPSHOT-native-zip.zip')),
      environment: {
        'spring_cloud_function_definition': 'aboutHandler'
      },
    });

    const fn = new lambda.Function(this, 'RandomBibleVerseFunction', {
      runtime: lambda.Runtime.PROVIDED,
      handler: 'functionRouter',
      code: lambda.Code.fromAsset(path.join(__dirname, 'data','lambda','bibleverse-0.0.1-SNAPSHOT-native-zip.zip')),
      environment: {
        'DATA_BUCKET_NAME': bucket.bucketName,
        'spring_cloud_function_definition': 'randomBibleVerseHandler'
      },
    });

  }
}
