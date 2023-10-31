package io.github.arthurpessoa.option

import org.apache.beam.sdk.options.Default
import org.apache.beam.sdk.options.Description
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.PipelineOptionsFactory

interface MovieCharacterOptions : PipelineOptions {
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

    @get:Description("input file")
    @get:Default.String("s3://mybucket/input/file1")
    var inputFile: String

    @get:Description("output file")
    @get:Default.String("s3://mybucket/output/file2.csv")
    var outputFile: String

    companion object {
        fun buildOptions(args: Array<String>): MovieCharacterOptions {
            PipelineOptionsFactory.register(MovieCharacterOptions::class.java)
            return PipelineOptionsFactory
                .fromArgs(*args)
                .create()
                .withS3PathStyle().`as`(MovieCharacterOptions::class.java)
        }
    }
}