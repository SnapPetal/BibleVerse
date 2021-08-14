const { AwsCdkTypeScriptApp, CdkApprovalLevel } = require('projen');
const project = new AwsCdkTypeScriptApp({
  cdkVersion: '1.118.0',
  defaultReleaseBranch: 'main',
  name: 'BibleVerse',
  cdkDependencies: ['@aws-cdk/aws-s3', '@aws-cdk/aws-s3-deployment'],
  requireApproval: CdkApprovalLevel.NEVER,
  gitignore: ['.DS_Store', 'cdk.context.json'],
});
project.synth();