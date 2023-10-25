package io.github.arthurpessoa

import java.io.Serializable


data class MovieCharacter(
    val id: Long,
    val name: String
) : Serializable