package io.github.arthurpessoa

import org.junit.jupiter.api.Assertions.assertEquals
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile.forHostPath
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import kotlin.test.Test

@Testcontainers
class SparkRunnerTest {

    private val network: Network = Network.newNetwork()

    @Container
    var localStack = LocalStackContainer(DockerImageName.parse("localstack/localstack:2.3.2"))
        .withNetwork(network)
        .withNetworkAliases("localstack")
        .withServices(S3)

    @Container
    var sparkContainer = GenericContainer(DockerImageName.parse("bitnami/spark:3.5.0"))
        .withCopyFileToContainer(forHostPath("build/resources/test/.", 484), "/home/")
        .withCopyFileToContainer(forHostPath("build/libs/.", 365), "/home/")
        .withNetwork(network)


    @Test
    fun `should run in a spark container`() {

        val s3Client = localStack
            .s3ClientBuilder()
            .build()

        s3Client.createBucket("balde")

        println(s3Client.listBuckets())

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

        println(s3Client.listObjects(ListObjectsRequest.builder().bucket("balde").build()))
        println(result.stdout)
        println(result.stderr)

        assertEquals(0, result.exitCode);
    }
}
