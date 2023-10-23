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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import kotlin.test.Test


@Testcontainers
class AppTest {

    private val network: Network = Network.newNetwork()

    @Container
    var s3Container = LocalStackContainer(DockerImageName.parse("localstack/localstack:2.3.2"))
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

        val s3Client = S3Client
            .builder()
            .endpointOverride(s3Container.getEndpointOverride(S3))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        s3Container.accessKey,
                        s3Container.secretKey
                    )
                )
            )
            .region(Region.of(s3Container.region))
            .build()


        s3Client.createBucket(
            CreateBucketRequest.builder()
                .bucket("balde")
                .build()
        )


        println(s3Client.listBuckets())
        val result = sparkContainer.execInContainer(
            "spark-submit",
            "--class", "io.github.arthurpessoa.PipelineKt",
            "--master", "local",
            "/home/sample-aggregation-all.jar",
            "--runner=SparkRunner",
            "--awsRegion=${s3Container.region}",
            "--endpoint=http://localstack:4566",
            """--awsCredentialsProvider={"@type": "StaticCredentialsProvider", "accessKeyId":"${s3Container.accessKey}", "secretAccessKey":"${s3Container.secretKey}"}""",
        )


        println(s3Client.listObjects(ListObjectsRequest.builder().bucket("balde").build()))
        println(result.stdout)
        println(result.stderr)

        assertEquals(0, result.exitCode);
    }
}
