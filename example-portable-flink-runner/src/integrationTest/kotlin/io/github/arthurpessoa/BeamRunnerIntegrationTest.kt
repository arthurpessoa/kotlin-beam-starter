package io.github.arthurpessoa


import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName.parse
import kotlin.test.Test

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

    @Test
    fun `should run in a flink container`() {

        val execInContainer = jobManagerContainer.execInContainer(
            "/opt/flink/bin/flink", "run",
            "-m", "flink-jobmanager:8081",
            "-c", "io.github.arthurpessoa.AppKt",
            "/jar/example-portable-flink-runner-all.jar"
        )
        print(execInContainer.stdout)
        print(execInContainer.stderr)

        taskManagerContainer.waitingFor(LogMessageWaitStrategy().withRegEx(".*APP FINISHED.*\\s"))
        println(taskManagerContainer.logs)
    }
}
