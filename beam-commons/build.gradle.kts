import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    `java-test-fixtures`

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.test.logger)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {

    runtimeOnly(libs.apache.beam.runners.direct.java)
    runtimeOnly(libs.bundles.logback)

    api(libs.apache.beam.sdks.java.io.amazon.web.services2)
    api(libs.apache.beam.sdks.java.io.jdbc)
    api(libs.apache.beam.sdks.java.core)
    api(libs.ojdbc11)

    testFixturesApi(libs.testcontainers.oracle.xe)
    testFixturesApi(libs.testcontainers.localstack)
    testFixturesApi(libs.testcontainers.kafka)

    testApi(libs.awssdk.s3)
    testApi(libs.junit)
    testApi(libs.kotlin.junit)
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

tasks.named<Test>("test") {
    useJUnitPlatform()
}
