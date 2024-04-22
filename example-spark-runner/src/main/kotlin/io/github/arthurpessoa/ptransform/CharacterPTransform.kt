package io.github.arthurpessoa.ptransform

import io.github.arthurpessoa.schema.Character
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.values.TypeDescriptor

fun mapStringIntoCharacter(): MapElements<String, Character> =
    MapElements
        .into(TypeDescriptor.of(Character::class.java))
        .via(SerializableFunction { line: String ->
            val (id, name) = line.split(",", ignoreCase = true)
            Character(id.toLong(), name)
        })