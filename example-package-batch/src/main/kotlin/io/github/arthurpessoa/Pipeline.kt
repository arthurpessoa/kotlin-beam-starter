package io.github.arthurpessoa

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.GroupByKey
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.transforms.windowing.FixedWindows
import org.apache.beam.sdk.transforms.windowing.Window
import org.apache.beam.sdk.values.KV
import org.apache.kafka.common.serialization.StringDeserializer
import org.joda.time.Duration
import org.joda.time.Instant


fun main(args: Array<String>) {

    val options = PackageOptions.buildOptions(args)
    val pipeline = Pipeline.create(options)
    println("START JOB")
    pipeline
        .apply(
            "ReadFromKafka", KafkaIO.read<String, String>()
                .withBootstrapServers(options.kafkaAddress)
                .withTopic(options.kafkaTopic)
                .withConsumerConfigUpdates(mapOf("group.id" to options.kafkaConsumerGroup))
                .withKeyDeserializer(StringDeserializer::class.java)
                .withValueDeserializer(StringDeserializer::class.java)
                .withStartReadTime(Instant.EPOCH) // Start reading from the earliest offset
                .withoutMetadata()
        )
        .apply(
            "Windowing", Window.into<KV<String, String>>(FixedWindows.of(Duration.standardMinutes(1)))
        )
        .apply("GroupByKey", GroupByKey.create())
        .apply("Print", ParDo.of(PrintFn()))


    pipeline.run()
        .waitUntilFinish()
}

class PrintFn<T> : DoFn<T, Void>() {
    @ProcessElement
    fun processElement(@Element element: T) {
        println("TOMB=$element")
    }
}