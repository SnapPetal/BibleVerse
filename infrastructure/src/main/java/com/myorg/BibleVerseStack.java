package com.myorg;

import java.util.ArrayList;
import java.util.List;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.BucketDeploymentProps;
import software.amazon.awscdk.services.s3.deployment.ISource;
import software.amazon.awscdk.services.s3.deployment.Source;

public class BibleVerseStack extends Stack {
	public BibleVerseStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public BibleVerseStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		Bucket bucket = new Bucket(this, "Bucket",
				new BucketProps.Builder().removalPolicy(RemovalPolicy.DESTROY).autoDeleteObjects(true).build());

		List<ISource> sources = new ArrayList<>(1);
		sources.add(Source.asset("../data"));

		BucketDeployment bucketDeployment = new BucketDeployment(this, "BucketDeployment",
				new BucketDeploymentProps.Builder().sources(sources).destinationBucket(bucket).build());
	}
}
