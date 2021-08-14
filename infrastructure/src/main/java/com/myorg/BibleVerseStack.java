package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.deployment.*;


public class BibleVerseStack extends Stack {
	public BibleVerseStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public BibleVerseStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		Bucket bucket = new Bucket(this, "Bucket",
				new BucketProps.Builder()
				.removalPolicy(RemovalPolicy.DESTROY)
				.autoDeleteObjects(true).build());
		
		BucketDeployment bucketDeployment = new BucketDeployment(this, "BucketDeployment", 
				new BucketDeploymentProps.Builder()
				.destinationBucket(bucket).build());
	}
}
