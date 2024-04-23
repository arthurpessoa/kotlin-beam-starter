package io.github.arthurpessoa


import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName.parse
import org.testcontainers.utility.MountableFile.forHostPath
import kotlin.test.Test

@Testcontainers
class SparkSubmitIntegrationTest {

    private val network: Network = Network.newNetwork()

    @Container
    val flinkJobManagerContainer: GenericContainer<*> = GenericContainer(parse("apache/flink:latest"))

        .withNetwork(network)
        .withCommand(
            "standalone-job",
            "--job-classname",
            "io.github.arthurpessoa.AppKt",
            "--jars",
            "/home/example-portable-flink-runner-all.jar",
        )
        .withCopyFileToContainer(forHostPath("build/libs/.", 365), "/home/")
        .withCopyFileToContainer(forHostPath("src/integrationTest/resources/flink", 365), "/")
        .withLogConsumer { frame ->
            println(frame.utf8String)
        }

    @Test
    fun `should run in a spark container`() {

        flinkJobManagerContainer.waitingFor(LogMessageWaitStrategy().withRegEx(".*APP FINISHED.*\\s"))
    }
}
