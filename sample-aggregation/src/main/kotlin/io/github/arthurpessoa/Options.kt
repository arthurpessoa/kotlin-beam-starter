package io.github.arthurpessoa

import org.apache.beam.sdk.io.aws2.common.ClientBuilderFactory
import org.apache.beam.sdk.io.aws2.options.S3Options
import org.apache.beam.sdk.io.aws2.s3.DefaultS3ClientBuilderFactory
import org.apache.beam.sdk.options.Default
import org.apache.beam.sdk.options.Description
import org.apache.beam.sdk.options.PipelineOptions
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder


interface MyOptions : PipelineOptions {
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
}


/*
*    FIXME: This config is only needed for testing, maybe the we should move it to a testFixture
*/
fun PipelineOptions.withS3PathStyle() = apply {
    `as`(S3Options::class.java).s3ClientFactoryClass = PathStyleS3ClientBuilderFactory::class.java
}

class PathStyleS3ClientBuilderFactory : DefaultS3ClientBuilderFactory() {

    override fun createBuilder(s3Options: S3Options): S3ClientBuilder {
        val builder = S3Client.builder().forcePathStyle(true)

        return ClientBuilderFactory.getFactory(s3Options).create<S3ClientBuilder, S3Client>(builder, s3Options)
    }
}