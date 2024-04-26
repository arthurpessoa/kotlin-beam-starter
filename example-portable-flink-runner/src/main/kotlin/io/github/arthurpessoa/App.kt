package io.github.arthurpessoa

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo

class PrintFn<T> : DoFn<T, Void>() {
    @ProcessElement
    fun processElement(@Element element: T) {
        println(element)
    }
}

fun main() {
    val create = Pipeline
        .create()

    create
        .apply(Create.of(1, 2, 3, 4, 5))
        .apply(ParDo.of(PrintFn<Int>()))

    create.run()
        .waitUntilFinish()


    println("APP FINISHED")
}



