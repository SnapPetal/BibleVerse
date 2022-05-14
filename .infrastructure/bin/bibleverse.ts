#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import {BibleVerseStack} from "../lib/bibleverse-stack";

const app = new cdk.App();
new BibleVerseStack(app, 'BibleVerseStack');
