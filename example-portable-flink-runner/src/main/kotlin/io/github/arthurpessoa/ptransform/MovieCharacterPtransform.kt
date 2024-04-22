package io.github.arthurpessoa.ptransform

import io.github.arthurpessoa.option.MovieCharacterOptions
import io.github.arthurpessoa.schema.MovieCharacter
import org.apache.beam.sdk.io.jdbc.JdbcIO
import org.apache.beam.sdk.schemas.transforms.Join
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.Row
import org.apache.beam.sdk.values.TypeDescriptor
import java.sql.PreparedStatement

fun mapJoinedRowIntoMovieCharacter(): MapElements<Row, MovieCharacter> =
    MapElements
        .into(TypeDescriptor.of(MovieCharacter::class.java))
        .via(SerializableFunction {

            //TODO: Find a better way to convert Row => Object
            val characterRow = it.getRow(Join.LHS_TAG)
            val movieRow = it.getRow(Join.RHS_TAG)

            MovieCharacter(
                characterRow?.getInt64("id"),
                characterRow?.getString("name"),
                movieRow?.getString("name")
            )
        })

fun writeToDatabase(options: MovieCharacterOptions) = JdbcIO.write<MovieCharacter>()
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
    .withStatement(
        """
        MERGE INTO MOVIE_CHARACTER USING dual ON ( character_id = ? )
            WHEN MATCHED THEN UPDATE SET character_name = ?
            WHEN NOT MATCHED THEN INSERT
                VALUES (?, ?)
        """
    )
    .withPreparedStatementSetter { movieCharacter: MovieCharacter, statement: PreparedStatement ->
        statement.setLong(1, movieCharacter.id!!)
        statement.setString(2, movieCharacter.characterName!!)
        statement.setLong(3, movieCharacter.id!!)
        statement.setString(4, movieCharacter.characterName!!)
    }