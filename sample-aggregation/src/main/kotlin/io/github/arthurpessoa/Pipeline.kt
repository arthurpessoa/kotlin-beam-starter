package io.github.arthurpessoa

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.aws2.common.ClientBuilderFactory
import org.apache.beam.sdk.io.aws2.options.S3Options
import org.apache.beam.sdk.io.aws2.s3.DefaultS3ClientBuilderFactory
import org.apache.beam.sdk.options.PipelineOptionsFactory
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder


fun main(args: Array<String>) {

    val options = PipelineOptionsFactory.fromArgs(*args).create().apply {
        //Força Usar o Path Style pra conseguir conectar no TestContainer
        `as`(S3Options::class.java).s3ClientFactoryClass = PathStyleS3ClientBuilderFactory::class.java
    }

    val pipeline = Pipeline.create(options)
    pipeline.catchPokemon()
    pipeline.run().waitUntilFinish()
}


fun Pipeline.catchPokemon() = this
    .apply("Read CSV1", readInitialFile("/home/file1.csv")).apply("Convert to Pokemon", convertToPokemon())
    .apply("Convert Pokemon to String", convertToString())
    .apply("save file", writeResultFile("s3://balde/file2").withSuffix(".csv"))

//TODO: Move me
class PathStyleS3ClientBuilderFactory : DefaultS3ClientBuilderFactory() {

    override fun createBuilder(s3Options: S3Options): S3ClientBuilder {
        val builder = S3Client.builder().forcePathStyle(true)

        return ClientBuilderFactory.getFactory(s3Options).create<S3ClientBuilder, S3Client>(builder, s3Options)
    }
}