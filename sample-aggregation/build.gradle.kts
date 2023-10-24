import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-test-fixtures`

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.shadow)
    alias(libs.plugins.test.logger)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {

    runtimeOnly(libs.apache.beam.runners.direct.java)
    runtimeOnly(libs.bundles.logback)

    api(libs.apache.beam.runners.spark)
    api(libs.apache.beam.sdks.java.io.amazon.web.services2)
    api(libs.apache.beam.sdks.java.core)

    //Oracle
    testApi(libs.testcontainers.oracle.xe)

    //LocalStack
    testFixturesApi(libs.testcontainers.localstack)
    testFixturesApi(libs.testcontainers.kafka)

    testApi(libs.awssdk.s3)
    testApi(libs.testcontainers.junit.jupiter)
    testApi(libs.junit)
    testApi(libs.kotlin.junit)
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
    maxHeapSize = "768m" //TODO: https://github.com/testcontainers/testcontainers-java/issues/4203
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    mustRunAfter(tasks["test"])
}

tasks.check {
    dependsOn(integrationTest)
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    isZip64 = true
}

