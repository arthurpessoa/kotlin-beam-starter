package io.github.arthurpessoa

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.schemas.transforms.Join
import org.apache.beam.sdk.values.PCollection

fun main(args: Array<String>) {

    PipelineOptionsFactory.register(MyOptions::class.java)
    val options = PipelineOptionsFactory.fromArgs(*args).create().withS3PathStyle().`as`(MyOptions::class.java)

    val pipeline = Pipeline.create(options)

    //Read The character CSV example
    val characters: PCollection<Character> = pipeline
        .apply("Read CSV file", readFile("${options.inputFile}_characters.csv"))
        .apply("Convert to Schema", mapStringIntoCharacter())

    //Read The Movie CSV example
    val movies: PCollection<Movie> = pipeline
        .apply("Read CSV file", readFile("${options.inputFile}_movies.csv"))
        .apply("Convert to Schema", mapStringIntoMovie())

    val movieCharacters: PCollection<MovieCharacter> =
        characters
            .apply(Join.innerJoin<Character, Movie>(movies).using("id"))
            .apply(mapJoinedRowIntoMovieCharacter())

    //Save CSV example
    movies.apply("Convert to String", mapMovieIntoString())
        .apply("save file", writeFile(options.outputFile))

    //Save to database Example
    movieCharacters.apply("Save to database", writeToDatabase(options))

    pipeline.run().waitUntilFinish()
}


