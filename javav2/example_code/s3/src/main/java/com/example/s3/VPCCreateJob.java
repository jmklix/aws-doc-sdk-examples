//snippet-sourcedescription:[VPCS3Example.java demonstrates how to create a S3ControlClient object using a virtual private cloud (VPC) URL.]
//snippet-keyword:[AWS SDK for Java v2]
//snippet-keyword:[Code Sample]
//snippet-service:[Amazon S3]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[1/19/2021]
//snippet-sourceauthor:[scmacdon-aws]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.example.s3;

// snippet-start:[s3.java2.create_job.vpc.import]
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3control.S3ControlClient;
import software.amazon.awssdk.services.s3control.model.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
// snippet-end:[s3.java2.create_job.vpc.import]


/**
 * To run this code example, ensure that you have followed the documentation provided here:
 *
 * https://docs.aws.amazon.com/AmazonS3/latest/dev/batch-ops-create-job.html
 *
 */
public class VPCCreateJob {

    public static void main(String[] args) throws URISyntaxException {

        final String USAGE = "\n" +
                "Usage:\n" +
                "    CreateJob <accountId> <iamRoleArn> <manifestLocation> <reportBucketName> <tagKey> <tagValue> <eTag> <vpcBucketURL>\n\n" +
                "Where:\n" +
                "    accountId - the account id value that owns the Amazon S3 bucket.\n\n" +
                "    iamRoleArn - the ARN of the AWS Identity and Access Management (IAM) role that has permissions to create a batch job.\n" +
                "    manifestLocation - the location where the manaifest file required for the job (for example, arn:aws:s3:::<BUCKETNAME>/manifest.csv).\n" +
                "    reportBucketName - the Amazon S3 bucket where the report is written to  (for example, arn:aws:s3:::<BUCKETNAME>).\n"+
                "    tagKey - the key used for a tag (for example,  keyOne).\n" +
                "    tagValue - the value for the key (for example,  ValueOne).\n" +
                "    eTag - the ETag for the specified manifest object (for example, 000000c9d1046e73f7dde5043ac3ae85).\n" +
                "    vpcBucketURL - the URL of the bucket located in your virtual private cloud (VPC) (for example,  https://bucket.vpce-xxxxxc4d-5e6f.s3.us-east-1.vpce.amazonaws.com)";

        if (args.length != 8) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String accountId = args[0];
        String iamRoleArn = args[1];
        String manifestLocation = args[2];
        String reportBucketName = args[3];
        String tagKey = args[4];
        String tagValue = args[5];
        String eTag = args [6];
        String vpcBucketURL = args[7];
        String uuid = java.util.UUID.randomUUID().toString();

        URI myURI = new URI(vpcBucketURL);
        S3ControlClient s3ControlClient = S3ControlClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(myURI)
                .build();

        createS3Job(s3ControlClient, accountId, iamRoleArn, manifestLocation, reportBucketName, tagKey, tagValue,eTag, uuid);
        s3ControlClient.close();
    }

    // snippet-start:[s3.java2.create_job.vpc.main]
    public static void createS3Job( S3ControlClient s3ControlClient,
                                    String accountId,
                                    String iamRoleArn,
                                    String manifestLocation,
                                    String reportBucketName,
                                    String tagKey,
                                    String tagValue,
                                    String eTag,
                                    String uuid  ) {

        try {
            ArrayList tagSet = new ArrayList<S3Tag>();

            S3Tag s3Tag = S3Tag.builder()
                    .key(tagKey)
                    .value(tagValue)
                    .build();

            tagSet.add(s3Tag);

            S3SetObjectTaggingOperation objectTaggingOperation = S3SetObjectTaggingOperation.builder()
                    .tagSet(s3Tag)
                    .build();

            JobOperation jobOperation = JobOperation.builder()
                    .s3PutObjectTagging(objectTaggingOperation)
                    .build();

            JobManifestLocation jobManifestLocation = JobManifestLocation.builder()
                    .objectArn(manifestLocation)
                    .eTag(eTag)
                    .build();

            JobManifestSpec manifestSpec = JobManifestSpec.builder()
                    .fieldsWithStrings(new String[]{"Bucket", "Key"})
                    .format(JobManifestFormat.S3_BATCH_OPERATIONS_CSV_20180820)
                    .build();

            JobManifest jobManifest = JobManifest.builder()
                    .spec(manifestSpec)
                    .location(jobManifestLocation)
                    .build();

            JobReport jobReport = JobReport.builder()
                    .bucket(reportBucketName)
                    .prefix("reports")
                    .format(JobReportFormat.REPORT_CSV_20180820)
                    .enabled(true)
                    .reportScope("AllTasks")
                    .build();

            CreateJobRequest jobRequest = CreateJobRequest.builder()
                    .accountId(accountId)
                    .description("Job created using the AWS Java SDK")
                    .manifest(jobManifest)
                    .operation(jobOperation)
                    .report(jobReport)
                    .priority(42)
                    .roleArn(iamRoleArn)
                    .clientRequestToken(uuid)
                    .confirmationRequired(false)
                    .build();

            s3ControlClient.createJob(jobRequest);

        } catch (S3ControlException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
    // snippet-end:[s3.java2.create_job.vpc.main]
}