package io.github.arthurpessoa


import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo


fun main(args: Array<String>) {

    val options = PackageOptions.buildOptions(args)
    val pipeline = Pipeline.create(options)
    println("START JOB")

    pipeline
        .apply("ReadFromKafka", readFromKafka(options))
        .apply("Windowing", window())
        .apply("BatchIntoMaxOf100", batch())
        .apply("PrintBatch", ParDo.of(PrintFn()))

    pipeline.run()
        .waitUntilFinish()
}

class PrintFn<T> : DoFn<T, Void>() {
    @ProcessElement
    fun processElement(@Element element: T) {

        println("BATCH=$element")
    }
}