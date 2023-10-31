package io.github.arthurpessoa

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.File

fun S3Client.createBucket(bucketName: String): CreateBucketResponse = createBucket(
    CreateBucketRequest.builder()
        .bucket(bucketName)
        .build()
)


fun S3Client.deleteBucket(bucketName: String): DeleteBucketResponse = deleteBucket(
    DeleteBucketRequest.builder()
        .bucket(bucketName)
        .build()
)


fun S3Client.deleteAllObjects(bucketName: String): List<DeleteObjectResponse> = listObjects(
    ListObjectsRequest.builder()
        .bucket(bucketName)
        .build()
).contents().map {
    deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(it.key())
            .build()
    )
}


fun S3Client.uploadFile(bucketName: String, fileName: String, filePathHost: String): PutObjectResponse = putObject(
    PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build(),
    RequestBody.fromFile(File(filePathHost))
)