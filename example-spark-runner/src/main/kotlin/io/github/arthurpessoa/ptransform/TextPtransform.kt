package io.github.arthurpessoa.ptransform

import org.apache.beam.sdk.io.Compression
import org.apache.beam.sdk.io.TextIO

fun readFile(filePattern: String): TextIO.Read =
    TextIO
        .read()
        .from(filePattern)

fun writeFile(filenamePrefix: String): TextIO.Write =
    TextIO
        .write()
        .to(filenamePrefix)
        .withSuffix(".csv")
        .withCompression(Compression.GZIP)