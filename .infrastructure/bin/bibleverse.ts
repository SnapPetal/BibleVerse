#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import {BibleVerseStack} from "../lib/bibleverse-stack";

const app = new cdk.App();
new BibleVerseStack(app, 'bible-verse-stack', {
    env: {
        account: '664759038511',
        region: 'us-east-1'
        
    }}
);
