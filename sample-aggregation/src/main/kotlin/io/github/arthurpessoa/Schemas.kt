package io.github.arthurpessoa

import org.apache.beam.sdk.schemas.JavaBeanSchema
import org.apache.beam.sdk.schemas.annotations.DefaultSchema
import java.io.Serializable



@DefaultSchema(JavaBeanSchema::class)
data class Movie(
    var id: Long? = null,
    var name: String? = null
) : Serializable

@DefaultSchema(JavaBeanSchema::class)
data class Character(
    var id: Long? = null,
    var name: String? = null
) : Serializable


@DefaultSchema(JavaBeanSchema::class)
data class MovieCharacter(
    var id: Long? = null,
    var characterName: String? = null,
    var movieName: String? = null
) : Serializable
