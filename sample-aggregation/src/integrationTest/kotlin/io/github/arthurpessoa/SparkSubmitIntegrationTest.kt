package io.github.arthurpessoa


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName.parse
import org.testcontainers.utility.MountableFile.forHostPath
import software.amazon.awssdk.services.s3.S3Client
import kotlin.test.Test


@Testcontainers
class SparkSubmitIntegrationTest {

    private val network: Network = Network.newNetwork()

    @Container
    val localStack: LocalStackContainer = LocalStackContainer(parse("localstack/localstack:2.3.2"))
        .withNetwork(network)
        .withNetworkAliases(LOCALSTACK_NETWORK)
        .withServices(S3)

    @Container
    val oracleContainer: OracleContainer = OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
        .withDatabaseName(DATABASE_NAME)
        .withUsername(DATABASE_USERNAME)
        .withPassword(DATABASE_PASSWORD)
        .withNetworkAliases(ORACLE_NETWORK)
        .withNetwork(network)

    @Container
    val sparkContainer: GenericContainer<*> = GenericContainer(parse("bitnami/spark:3.5.0"))
        .withCopyFileToContainer(forHostPath("build/libs/.", 365), "/home/")
        .withNetwork(network)

/*
    @Container
    val kafkaContainer: KafkaContainer = KafkaContainer(parse("confluentinc/cp-kafka:6.2.1"))
        .withNetwork(network)*/

    lateinit var s3Client: S3Client

    @Test
    fun `should run in a spark container`() {

        s3Client.uploadFile(BUCKET_NAME, "input/file1.csv", "src/test/resources/file1.csv")

        val result = sparkContainer.execInContainer(
            "spark-submit",
            "--class", "io.github.arthurpessoa.PipelineKt",
            "--master", "local",
            "/home/sample-aggregation-all.jar",
            "--runner=SparkRunner",
            "--awsRegion=${localStack.region}",
            "--endpoint=http://$LOCALSTACK_NETWORK:4566",
            """--awsCredentialsProvider={"@type": "StaticCredentialsProvider", "accessKeyId":"${localStack.accessKey}", "secretAccessKey":"${localStack.secretKey}"}""",
            "--dbName=$DATABASE_NAME",
            "--dbUsername=$DATABASE_USERNAME",
            "--dbPassword=$DATABASE_PASSWORD",
            "--dbUrl=jdbc:oracle:thin:@$ORACLE_NETWORK:${oracleContainer.oraclePort}/$DATABASE_NAME",
        )

        println(result.stdout)
        println(result.stderr)

        assertEquals(0, result.exitCode)
    }

    @BeforeEach
    fun beforeEach() {
        s3Client = localStack.s3ClientBuilder().build()
        s3Client.createBucket(BUCKET_NAME)

        oracleContainer.connection.use {
            it.createStatement().execute(STATEMENT_CREATE_TABLE)
        }
    }

    @AfterEach
    fun afterEach() {
        s3Client.deleteAllObjects(BUCKET_NAME)
        s3Client.deleteBucket(BUCKET_NAME)
    }

    companion object {
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
                CREATE TABLE MOVIE_CHARACTER (
                    character_id NUMBER PRIMARY KEY,
                    character_name VARCHAR2(50) NOT NULL
                )
            """

    }

}
