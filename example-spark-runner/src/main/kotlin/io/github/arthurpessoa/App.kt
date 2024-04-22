package io.github.arthurpessoa

import io.github.arthurpessoa.option.MovieCharacterOptions
import io.github.arthurpessoa.pipeline.moviePipeline
import org.apache.beam.sdk.Pipeline

fun main(args: Array<String>) {
    Pipeline
        .create(MovieCharacterOptions.buildOptions(args))
        .apply {
            moviePipeline()
        }
        .run()
        .waitUntilFinish()
}



