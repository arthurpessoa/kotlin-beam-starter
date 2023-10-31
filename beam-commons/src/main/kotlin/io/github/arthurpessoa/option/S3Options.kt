package io.github.arthurpessoa.option

import org.apache.beam.sdk.io.aws2.common.ClientBuilderFactory
import org.apache.beam.sdk.io.aws2.options.S3Options
import org.apache.beam.sdk.io.aws2.s3.DefaultS3ClientBuilderFactory
import org.apache.beam.sdk.options.PipelineOptions
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder

fun PipelineOptions.withS3PathStyle() = apply {
    `as`(S3Options::class.java).s3ClientFactoryClass = PathStyleS3ClientBuilderFactory::class.java
}

class PathStyleS3ClientBuilderFactory : DefaultS3ClientBuilderFactory() {

    override fun createBuilder(s3Options: S3Options): S3ClientBuilder {
        val builder = S3Client.builder().forcePathStyle(true)

        return ClientBuilderFactory.getFactory(s3Options).create<S3ClientBuilder, S3Client>(builder, s3Options)
    }
}