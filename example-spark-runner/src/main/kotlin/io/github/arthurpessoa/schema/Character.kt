package io.github.arthurpessoa.schema

import org.apache.beam.sdk.schemas.JavaBeanSchema
import org.apache.beam.sdk.schemas.annotations.DefaultSchema
import java.io.Serializable

@DefaultSchema(JavaBeanSchema::class)
data class Character(
    var id: Long? = null,
    var name: String? = null
) : Serializable