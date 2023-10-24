package io.github.arthurpessoa

import java.io.Serializable


data class MySchema(
    val id: Long,
    val name: String
) : Serializable