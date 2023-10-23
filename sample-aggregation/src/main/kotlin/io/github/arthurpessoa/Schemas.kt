package io.github.arthurpessoa

import org.apache.beam.sdk.schemas.JavaFieldSchema
import org.apache.beam.sdk.schemas.annotations.DefaultSchema


@DefaultSchema(JavaFieldSchema::class)
data class Pokemon(val name: String)