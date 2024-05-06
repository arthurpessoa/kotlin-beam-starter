package io.github.arthurpessoa

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.*
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName.parse
import software.amazon.awssdk.services.s3.S3Client


@Testcontainers
open class BaseIntegrationTest {

    private val network: Network = Network.newNetwork()

//    @Container
//    val oracleContainer: OracleContainer = OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
//        .withDatabaseName(DATABASE_NAME)
//        .withUsername(DATABASE_USERNAME)
//        .withPassword(DATABASE_PASSWORD)
//        .withNetworkAliases(ORACLE_NETWORK)
//        .withNetwork(network)

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
    val localStack: LocalStackContainer = LocalStackContainer(parse("localstack/localstack:2.3.2"))
        .withNetwork(network)
        .withNetworkAliases(LOCALSTACK_NETWORK)
        .withServices(S3)

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

            "--kafkaAddress=kafka:9092", //Kafka
            "--kafkaTopic=mytopic",

            "--dbName=$DATABASE_NAME", //Database
            "--dbUsername=$DATABASE_USERNAME",
            "--dbPassword=$DATABASE_PASSWORD",
            "--dbUrl=jdbc:oracle:thin:@$ORACLE_NETWORK:1521/$DATABASE_NAME"
        )
        .withEnv("FLINK_PROPERTIES", FLINK_PROPERTIES)
        .withLogConsumer { frame ->
            println("App>> ${frame.utf8String}")
        }
        .dependsOn(
            kafkaContainer,
            partitionCreation,
            jobManagerContainer,
            taskManagerContainer,
            localStack
        )

//    lateinit var s3Client: S3Client

/*    @BeforeEach
    fun beforeEach() {
        s3Client = localStack.s3ClientBuilder().build()
        s3Client.createBucket(BUCKET_NAME)

        oracleContainer.connection.use {
            it.createStatement().execute(STATEMENT_CREATE_TABLE)
        }
    }*/

/*    @AfterEach
    fun afterEach() {
        s3Client.deleteAllObjects(BUCKET_NAME)
        s3Client.deleteBucket(BUCKET_NAME)
    }*/

    companion object {

        // Flink
        const val FLINK_PROPERTIES =
            """jobmanager.rpc.address:flink-jobmanager
               parallelism.default:1
               taskmanager.numberOfTaskSlots:1"""

        // S3
        const val BUCKET_NAME = "mybucket"
        const val LOCALSTACK_NETWORK = "localstack"

        // Database
        const val ORACLE_NETWORK = "database"
        const val DATABASE_NAME = "db"
        const val DATABASE_USERNAME = "dbuser"
        const val DATABASE_PASSWORD = "dbpw"
        const val STATEMENT_CREATE_TABLE =
            """
                CREATE TABLE PACKAGE (
                    package_id VARCHAR(255) PRIMARY KEY
                )
            """
    }
}