package io.github.arthurpessoa

import org.testcontainers.containers.OracleContainer
import java.sql.Connection
import java.sql.DriverManager

val OracleContainer.connection: Connection
    get() = DriverManager.getConnection(
        "jdbc:oracle:thin:@localhost:${oraclePort}/$databaseName",
        username,
        password
    )
