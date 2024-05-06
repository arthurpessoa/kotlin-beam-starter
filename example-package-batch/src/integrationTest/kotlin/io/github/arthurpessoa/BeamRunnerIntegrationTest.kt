package io.github.arthurpessoa


import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.Test


class SparkSubmitIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `should run in a flink container`() {

        val producer = KafkaProducer<String, String>(Properties().apply {
            put("bootstrap.servers", kafkaContainer.bootstrapServers)
            put("key.serializer", StringSerializer::class.java.canonicalName)
            put("value.serializer", StringSerializer::class.java.canonicalName)
        })


        (1..10).map {
            producer.send(ProducerRecord("mytopic", "BRAZIL", "PACKAGE $it"))
        }.forEach { it.get() }

        producer.send(ProducerRecord("mytopic", "BRAZIL", "PACKAGE 11"))

        Awaitility.await().atMost(600, TimeUnit.SECONDS).until {
            appContainer.logs.contains("PACKAGE 11")
        }
    }
}
