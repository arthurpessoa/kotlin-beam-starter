package io.github.arthurpessoa

import org.apache.beam.sdk.io.Compression
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
        .withSuffix(".csv")
        .withCompression(Compression.GZIP)
/*
 * TODO: Dá pra fazer um nome custom do arquivo output, pra não haver a necessidade de mover o arquivo depois
 * https://stackoverflow.com/questions/46638425/does-apache-beam-support-custom-file-names-for-its-output
 */