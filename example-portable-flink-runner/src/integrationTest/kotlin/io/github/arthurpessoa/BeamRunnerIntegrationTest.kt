package io.github.arthurpessoa


import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName.parse
import kotlin.test.Test
import kotlin.test.assertEquals

@Testcontainers
class SparkSubmitIntegrationTest {

    private val network: Network = Network.newNetwork()

    @Container
    val jobManagerContainer: GenericContainer<*> = GenericContainer(parse("apache/flink:latest"))
        .withNetwork(network)
        .withCommand("jobmanager")
        .withFileSystemBind("build/libs/", "/jar", BindMode.READ_ONLY)
        .withNetworkAliases("flink-jobmanager")
        .withLogConsumer { frame ->
            println(frame.utf8String)
        }

    @Container
    val taskManagerContainer: GenericContainer<*> = GenericContainer(parse("apache/flink:latest"))
        .withNetwork(network)
        .withCommand("taskmanager")
        .withEnv("FLINK_PROPERTIES", "jobmanager.rpc.address: flink-jobmanager")
        .dependsOn(jobManagerContainer)
        .withLogConsumer { frame ->
            println(frame.utf8String)
        }

    @Test
    fun `should run in a flink container`() {

        val jobExecution = jobManagerContainer.execInContainer(
            "/opt/flink/bin/flink", "run",
            "-m", "flink-jobmanager:8081",
            "-c", "io.github.arthurpessoa.AppKt",
            "/jar/example-portable-flink-runner-all.jar"
        )

        println(jobExecution.stdout)
        println(jobExecution.stderr)

        assertEquals(jobExecution.exitCode, 0)
        println(taskManagerContainer.logs)
    }
}
