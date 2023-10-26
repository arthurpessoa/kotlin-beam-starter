package io.github.arthurpessoa

import org.apache.beam.sdk.io.Compression
import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.io.jdbc.JdbcIO
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.values.TypeDescriptor
import org.apache.beam.sdk.values.TypeDescriptors
import java.sql.PreparedStatement
import org.apache.beam.sdk.transforms.SerializableFunction as func

fun readFile(filePattern: String): TextIO.Read =
    TextIO
        .read()
        .from(filePattern)

fun convertToMovieCharacter(): MapElements<String, MovieCharacter> =
    MapElements
        .into(TypeDescriptor.of(MovieCharacter::class.java))
        .via(func { line: String ->
            val (id, name) = line.split(",", ignoreCase = true)
            MovieCharacter(id.toLong(), name)
        })

fun convertFromMovieCharacter(): MapElements<MovieCharacter, String> =
    MapElements
        .into(TypeDescriptors.strings())
        .via(func { movieCharacter: MovieCharacter ->
            movieCharacter.name
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
        statement.setLong(1, movieCharacter.id)
        statement.setString(2, movieCharacter.name)
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