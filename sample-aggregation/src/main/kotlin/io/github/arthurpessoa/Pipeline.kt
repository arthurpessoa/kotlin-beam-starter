package io.github.arthurpessoa

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.values.PCollection

fun main(args: Array<String>) {

    PipelineOptionsFactory.register(MyOptions::class.java)
    val options = PipelineOptionsFactory
        .fromArgs(*args)
        .create()
        .withS3PathStyle()
        .`as`(MyOptions::class.java)

    val pipeline = Pipeline.create(options)

    //Read CSV example
    val movieCharacter: PCollection<MovieCharacter> = pipeline
        .apply("Read CSV file", readFile(options.inputFile))
        .apply("Convert to Schema", convertToMovieCharacter())

    //Save CSV example
    movieCharacter
        .apply("Convert to String", convertFromMovieCharacter())
        .apply("save file", writeFile(options.outputFile))

    //Save to database Example
    movieCharacter
        .apply("Save to database", writeToDatabase(options))

    pipeline.run().waitUntilFinish()
}


