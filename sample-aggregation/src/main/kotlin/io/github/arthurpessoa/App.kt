package io.github.arthurpessoa

import io.github.arthurpessoa.option.buildOptions
import io.github.arthurpessoa.pipeline.moviePipeline
import org.apache.beam.sdk.Pipeline

fun main(args: Array<String>) {
    Pipeline
        .create(buildOptions(args))
        .apply {
            moviePipeline()
        }
        .run()
        .waitUntilFinish()
}



