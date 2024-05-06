package io.github.arthurpessoa

import org.apache.beam.sdk.io.kafka.KafkaIO
import org.apache.beam.sdk.transforms.GroupIntoBatches
import org.apache.beam.sdk.transforms.windowing.*
import org.apache.beam.sdk.values.KV
import org.apache.kafka.common.serialization.StringDeserializer
import org.joda.time.Duration
import org.joda.time.Instant

fun window() = Window.into<KV<String, String>>(FixedWindows.of(Duration.standardSeconds(10)))
    .triggering(
        Repeatedly.forever(
            AfterWatermark.pastEndOfWindow()
                .withEarlyFirings(AfterPane.elementCountAtLeast(2))
                .withLateFirings(AfterPane.elementCountAtLeast(1))
        )
    )
    .withAllowedLateness(Duration.standardDays(1))
    .discardingFiredPanes()

fun readFromKafka(options: PackageOptions) = KafkaIO.read<String, String>()
    .withBootstrapServers(options.kafkaAddress)
    .withTopic(options.kafkaTopic)
    .withConsumerConfigUpdates(mapOf("group.id" to options.kafkaConsumerGroup))
    .withKeyDeserializer(StringDeserializer::class.java)
    .withValueDeserializer(StringDeserializer::class.java)
    .commitOffsetsInFinalize()
    .withStartReadTime(Instant.EPOCH)
    .withoutMetadata()

fun batch() = GroupIntoBatches
    .ofSize<String, String>(2)
    .withMaxBufferingDuration(Duration.standardSeconds(10))