import * as cdk from 'aws-cdk-lib';
import {Template} from 'aws-cdk-lib/assertions';
import {BibleVerseStack} from "../lib/bibleverse-stack";

test('S3 Bucket Created', () => {
    const app = new cdk.App();
    const stack = new BibleVerseStack(app, 'MyTestStack');
    const template = Template.fromStack(stack);

    template.hasResourceProperties('AWS::S3::Bucket', {
        "PublicAccessBlockConfiguration": {
            "BlockPublicAcls": true, "BlockPublicPolicy": true, "IgnorePublicAcls": true, "RestrictPublicBuckets": true
        },
    });
});
