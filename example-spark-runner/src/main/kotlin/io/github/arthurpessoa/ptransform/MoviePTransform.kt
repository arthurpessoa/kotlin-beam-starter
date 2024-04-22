package io.github.arthurpessoa.ptransform

import io.github.arthurpessoa.schema.Movie
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.TypeDescriptor
import org.apache.beam.sdk.values.TypeDescriptors

fun mapStringIntoMovie(): MapElements<String, Movie> =
    MapElements
        .into(TypeDescriptor.of(Movie::class.java))
        .via(SerializableFunction { line: String ->
            val (id, name) = line.split(",", ignoreCase = true)
            Movie(id.toLong(), name)
        })

fun mapMovieIntoString(): MapElements<Movie, String> =
    MapElements
        .into(TypeDescriptors.strings())
        .via(SerializableFunction { movie: Movie ->
            movie.name
        })