package io.github.arthurpessoa

import org.apache.beam.sdk.options.Default
import org.apache.beam.sdk.options.Description
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.PipelineOptionsFactory

interface PackageOptions : PipelineOptions {
    @get:Description("Kafka Server Address")
    @get:Default.String("localhost:9092")
    var kafkaAddress: String

    @get:Description("Kafka Topic")
    @get:Default.String("mytopic")
    var kafkaTopic: String

    @get:Description("Kafka Consumer Group")
    @get:Default.String("beam-consumer-group")
    var kafkaConsumerGroup: String

    @get:Description("Database Name")
    @get:Default.String("db")
    var dbName: String

    @get:Description("Database Username")
    @get:Default.String("username")
    var dbUsername: String

    @get:Description("Database Password")
    @get:Default.String("password")
    var dbPassword: String

    @get:Description("Database Driver")
    @get:Default.String("oracle.jdbc.driver.OracleDriver")
    var dbDriver: String

    @get:Description("Database Connection Url")
    @get:Default.String("jdbc:oracle:thin:@localhost:1521/db")
    var dbUrl: String

    companion object {
        fun buildOptions(args: Array<String>): PackageOptions {
            PipelineOptionsFactory.register(PackageOptions::class.java)
            return PipelineOptionsFactory
                .fromArgs(*args)
                .create()
                .`as`(PackageOptions::class.java)
        }
    }
}