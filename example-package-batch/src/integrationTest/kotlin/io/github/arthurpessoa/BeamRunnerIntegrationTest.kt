package io.github.arthurpessoa


import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName.parse
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.Test


const val FLINK_PROPERTIES =
    """jobmanager.rpc.address:flink-jobmanager
       parallelism.default:1
       taskmanager.numberOfTaskSlots:1"""

@Testcontainers
class SparkSubmitIntegrationTest {

    private val network: Network = Network.newNetwork()


    @Container
    val kafkaContainer: KafkaContainer = KafkaContainer(parse("confluentinc/cp-kafka:7.3.0"))
        .withNetwork(network)
        .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
        .withNetworkAliases("kafka")
        .withLogConsumer { frame ->
            println("kafka>> ${frame.utf8String}")
        }
        .withKraft()
        .waitingFor(Wait.forLogMessage(".*Kafka Server started.*", 1))


    @Container
    val partitionCreation: GenericContainer<*> = GenericContainer(parse("confluentinc/cp-kafka:7.3.0"))
        .withNetwork(network)
        .withCommand(
            "/usr/bin/kafka-topics",
            "--create",
            "--topic",
            "mytopic",
            "--partitions",
            "1",
            "--replication-factor",
            "1",
            "--bootstrap-server",
            "kafka:9092"
        )
        .withLogConsumer { frame ->
            println("partition>> ${frame.utf8String}")
        }
        .dependsOn(kafkaContainer)

    @Container
    val jobManagerContainer: GenericContainer<*> = GenericContainer(parse("apache/flink:latest"))
        .withNetwork(network)
        .withCommand("jobmanager")
        .withNetworkAliases("flink-jobmanager")
        .withEnv("FLINK_PROPERTIES", FLINK_PROPERTIES)
        .withLogConsumer { frame ->
            println("JobManager>> ${frame.utf8String}")
        }
        .waitingFor(Wait.forLogMessage(".*Starting the slot manager.*", 1))

    @Container
    val taskManagerContainer: GenericContainer<*> = GenericContainer(parse("apache/flink:latest"))
        .withNetwork(network)
        .withCommand("taskmanager")
        .withEnv("FLINK_PROPERTIES", FLINK_PROPERTIES)
        .withLogConsumer { frame ->
            println("Taskmanager>> ${frame.utf8String}")
        }
        .waitingFor(Wait.forLogMessage(".*Successful registration.*", 1))


    @Container
    val appContainer: GenericContainer<*> = GenericContainer(parse("apache/flink:latest"))
        .withNetwork(network)
        .withCommand("jobmanager")
        .withFileSystemBind("build/libs/", "/jar", BindMode.READ_ONLY)
        .withNetworkAliases("flink-app")
        .withCommand(
            "/opt/flink/bin/flink", "run",
            "-m", "flink-jobmanager:8081",
            "/jar/example-package-batch-all.jar",
            "--kafkaAddress=kafka:9092",
            "--kafkaTopic=mytopic",
        )
        .withEnv("FLINK_PROPERTIES", FLINK_PROPERTIES)
        .withLogConsumer { frame ->
            println("App>> ${frame.utf8String}")
        }
        .dependsOn(kafkaContainer, partitionCreation, jobManagerContainer, taskManagerContainer)

    @Test
    fun `should run in a flink container`() {


        val producer = KafkaProducer<String, String>(Properties().apply {
            put("bootstrap.servers", kafkaContainer.bootstrapServers)
            put("key.serializer", StringSerializer::class.java.canonicalName)
            put("value.serializer", StringSerializer::class.java.canonicalName)
        })


        producer.send(ProducerRecord("mytopic", "key1", "MESSAGE 1")).get()
        producer.send(ProducerRecord("mytopic", "key1", "MESSAGE 2")).get()
        producer.send(ProducerRecord("mytopic", "key2", "MESSAGE 3")).get()

        Awaitility.await().atMost(600, TimeUnit.SECONDS).until {
            appContainer.logs.contains("MESSAGE 3")
        }

        producer.send(ProducerRecord("mytopic", "key1", "MESSAGE 4")).get()

        Awaitility.await().atMost(600, TimeUnit.SECONDS).until {
            appContainer.logs.contains("MESSAGE 4")
        }


    }
}
