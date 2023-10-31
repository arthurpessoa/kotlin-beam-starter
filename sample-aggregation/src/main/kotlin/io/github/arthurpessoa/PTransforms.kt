package io.github.arthurpessoa

import org.apache.beam.sdk.io.Compression
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.io.jdbc.JdbcIO
import org.apache.beam.sdk.schemas.transforms.Join
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.values.Row
import org.apache.beam.sdk.values.TypeDescriptor
import org.apache.beam.sdk.values.TypeDescriptors
import java.sql.PreparedStatement
import org.apache.beam.sdk.transforms.SerializableFunction as func

fun readFile(filePattern: String): TextIO.Read =
    TextIO
        .read()
        .from(filePattern)

fun mapStringIntoMovie(): MapElements<String, Movie> =
    MapElements
        .into(TypeDescriptor.of(Movie::class.java))
        .via(func { line: String ->
            val (id, name) = line.split(",", ignoreCase = true)
            Movie(id.toLong(), name)
        })


fun mapStringIntoCharacter(): MapElements<String, Character> =
    MapElements
        .into(TypeDescriptor.of(Character::class.java))
        .via(func { line: String ->
            val (id, name) = line.split(",", ignoreCase = true)
            Character(id.toLong(), name)
        })

fun mapJoinedRowIntoMovieCharacter(): MapElements<Row, MovieCharacter> =
    MapElements
        .into(TypeDescriptor.of(MovieCharacter::class.java))
        .via(func {

            //TODO: Find a better way to convert Row => Object
            val characterRow = it.getRow(Join.LHS_TAG)
            val movieRow = it.getRow(Join.RHS_TAG)

            MovieCharacter(
                characterRow?.getInt64("id"),
                characterRow?.getString("name"),
                movieRow?.getString("name")
            )
        })


fun mapMovieIntoString(): MapElements<Movie, String> =
    MapElements
        .into(TypeDescriptors.strings())
        .via(func { movie: Movie ->
            movie.name
        })


fun writeToDatabase(options: MyOptions) = JdbcIO.write<MovieCharacter>()
    .withDataSourceConfiguration(
        JdbcIO.DataSourceConfiguration
            .create(
                options.dbDriver,
                options.dbUrl
            )
            .withMaxConnections(10)
            .withUsername(options.dbUsername)
            .withPassword(options.dbPassword)
    )
    .withBatchSize(500)
    .withStatement("INSERT into MOVIE_CHARACTER values(?, ?)")
    .withPreparedStatementSetter { movieCharacter: MovieCharacter, statement: PreparedStatement ->

        statement.setLong(1, movieCharacter.id!!)
        statement.setString(2, movieCharacter.characterName!!)
    }

fun writeFile(filenamePrefix: String): TextIO.Write =
    TextIO
        .write()
        .to(filenamePrefix)
        .withSuffix(".csv")
        .withCompression(Compression.GZIP)
/*
 * TODO: Dá pra fazer um nome custom do arquivo output, pra não haver a necessidade de mover o arquivo depois
 * https://stackoverflow.com/questions/46638425/does-apache-beam-support-custom-file-names-for-its-output
 */