import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-test-fixtures`

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {

    runtimeOnly(libs.apache.beam.runners.direct.java)
    api(libs.apache.beam.runners.spark)
    api(libs.apache.beam.sdks.java.io.amazon.web.services2)
    api(libs.apache.beam.sdks.java.core)


    //Oracle
    testApi(libs.testcontainers.oracle.xe)

    //LocalStack
    testFixturesApi(libs.testcontainers.localstack)


    testApi(libs.testcontainers.junit.jupiter)
    testApi("software.amazon.awssdk:s3:2.21.5")
    testApi(libs.junit)
    testApi(libs.kotlin.junit)
    testApi(libs.bundles.awaitility)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn("shadowJar")
    maxHeapSize = "768m" //TODO: https://github.com/testcontainers/testcontainers-java/issues/4203
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    isZip64 = true
}
