package io.github.arthurpessoa.pipeline

import io.github.arthurpessoa.option.MovieCharacterOptions
import io.github.arthurpessoa.ptransform.*
import io.github.arthurpessoa.schema.Character
import io.github.arthurpessoa.schema.Movie
import io.github.arthurpessoa.schema.MovieCharacter
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.schemas.transforms.Join
import org.apache.beam.sdk.values.PCollection

fun Pipeline.moviePipeline() {

    val options = this.options as MovieCharacterOptions

    //Read The character CSV example
    val characters: PCollection<Character> = this
        .apply("Read Characters file", readFile("${options.inputFile}_characters.csv"))
        .apply("Map String into Character", mapStringIntoCharacter())

    //Read The Movie CSV example
    val movies: PCollection<Movie> = this
        .apply("Read Movies file", readFile("${options.inputFile}_movies.csv"))
        .apply("Map String into Movie", mapStringIntoMovie())

    val movieCharacters: PCollection<MovieCharacter> =
        characters
            .apply("Join Character and Movie", Join.innerJoin<Character, Movie>(movies).using("id"))
            .apply("Map Join Row Into a MovieCharacter", mapJoinedRowIntoMovieCharacter())

    //Save CSV example
    movies
        .apply("Map Movie into String", mapMovieIntoString())
        .apply("Write Movie String into a File", writeFile(options.outputFile))

    //Save to database Example
    movieCharacters.apply("Write MovieCharacter into Database", writeToDatabase(options))
}