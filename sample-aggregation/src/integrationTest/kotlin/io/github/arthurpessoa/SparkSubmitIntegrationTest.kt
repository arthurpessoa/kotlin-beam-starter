package io.github.arthurpessoa


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.Network
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
        .withNetworkAliases("localstack")
        .withServices(S3)

    @Container
    val sparkContainer: GenericContainer<*> = GenericContainer(parse("bitnami/spark:3.5.0"))
        .withCopyFileToContainer(forHostPath("build/libs/.", 365), "/home/")
        .withNetwork(network)

    @Container
    val kafkaContainer: KafkaContainer = KafkaContainer(parse("confluentinc/cp-kafka:6.2.1"))
        .withNetwork(network)


    lateinit var s3Client: S3Client

    @Test
    fun `should run in a spark container`() {


        s3Client.uploadFile(bucketName, "input/file1.csv", "src/test/resources/file1.csv")

        val result = sparkContainer.execInContainer(
            "spark-submit",
            "--class", "io.github.arthurpessoa.PipelineKt",
            "--master", "local",
            "/home/sample-aggregation-all.jar",
            "--runner=SparkRunner",
            "--awsRegion=${localStack.region}",
            "--endpoint=http://localstack:4566",
            """--awsCredentialsProvider={"@type": "StaticCredentialsProvider", "accessKeyId":"${localStack.accessKey}", "secretAccessKey":"${localStack.secretKey}"}""",
        )

        println(result.stdout)
        println(result.stderr)

        assertEquals(0, result.exitCode)
    }

    @BeforeEach
    fun beforeEach() {
        s3Client = localStack.s3ClientBuilder().build()
        s3Client.createBucket(bucketName)
    }

    @AfterEach
    fun afterEach() {
        s3Client.deleteAllObjects(bucketName)
        s3Client.deleteBucket(bucketName)
    }

    companion object {
        const val bucketName = "mybucket"
    }
}
