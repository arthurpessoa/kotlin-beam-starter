package io.github.arthurpessoa

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory

fun main(args: Array<String>) {

    val options = PipelineOptionsFactory
        .fromArgs(*args)
        .create()
        .withS3PathStyle()

    val pipeline = Pipeline.create(options)
    pipeline
        .apply("Read CSV1", readInitialFile("s3://mybucket/input/file1.csv"))
        .apply("Convert to Schema", convertToSchema())
        .apply("Convert to String", convertToString())
        .apply("save file", writeResultFile("s3://mybucket/output/file2"))

    pipeline.run().waitUntilFinish()
}

