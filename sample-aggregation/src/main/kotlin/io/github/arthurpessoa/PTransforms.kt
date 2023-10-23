package io.github.arthurpessoa

import org.apache.beam.sdk.io.TextIO
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.values.TypeDescriptor
import org.apache.beam.sdk.values.TypeDescriptors
import org.apache.beam.sdk.transforms.SerializableFunction as func

fun readInitialFile(filePattern: String): TextIO.Read =
    TextIO
        .read()
        .from(filePattern)

fun convertToPokemon(): MapElements<String, Pokemon> =
    MapElements
        .into(TypeDescriptor.of(Pokemon::class.java))
        .via(func { name: String ->
            Pokemon(name)
        })

fun convertToString(): MapElements<Pokemon, String> =
    MapElements
        .into(TypeDescriptors.strings())
        .via(func { pokemon: Pokemon ->
            pokemon.name
        })

fun writeResultFile(filenamePrefix: String): TextIO.Write =
    TextIO
        .write()
        .to(filenamePrefix)