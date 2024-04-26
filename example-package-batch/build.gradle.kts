import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-test-fixtures`
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.shadow)
    alias(libs.plugins.test.logger)
}

application {
    mainClass = "io.github.arthurpessoa.PipelineKt"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {

    runtimeOnly(libs.apache.beam.runners.direct.java)
    runtimeOnly(libs.bundles.logback)

    api(libs.apache.beam.runners.portability.java)
    api(libs.apache.beam.sdks.java.io.amazon.web.services2)
    api(libs.apache.beam.sdks.java.io.jdbc)
    api(libs.apache.beam.sdks.java.io.kafka)
    api(libs.apache.beam.sdks.java.core)
    api(libs.apache.kafka.clients)

    api(project(":beam-commons"))
    api(libs.ojdbc11)

    testFixturesApi(libs.testcontainers.oracle.xe)
    testFixturesApi(libs.testcontainers.localstack)
    testFixturesApi(libs.testcontainers.kafka)

    testApi(testFixtures(project(":beam-commons")))

    testApi(libs.awssdk.s3)
    testApi(libs.apache.flink.test.utils)
    testApi(libs.apache.flink.test.utils.junit)
    testApi(libs.testcontainers.junit.jupiter)
    testApi(libs.junit)
    testApi(libs.kotlin.junit)
    testApi("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testApi(libs.bundles.awaitility)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

testlogger {
    theme = ThemeType.MOCHA
    slowThreshold = 5000
}

sourceSets {
    create("integrationTest") {

        kotlin {
            compileClasspath += main.get().output + configurations.testRuntimeClasspath.get()
            runtimeClasspath += output + compileClasspath
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn("shadowJar")
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    dependsOn("build")
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    isZip64 = true
}

