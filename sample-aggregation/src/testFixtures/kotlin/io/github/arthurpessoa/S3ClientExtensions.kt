package io.github.arthurpessoa

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.CreateBucketResponse

fun S3Client.createBucket(bucketName: String): CreateBucketResponse = createBucket(
    CreateBucketRequest.builder()
        .bucket(bucketName)
        .build()
)