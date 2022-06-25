#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import {BibleVerseStack} from "../lib/bibleverse-stack";

const app = new cdk.App();
new BibleVerseStack(app, 'bible-verse-stack', {
    env: {
        account: process.env.CDK_DEFAULT_ACCOUNT,
        region: process.env.CDK_DEFAULT_REGION
        
    }}
);
