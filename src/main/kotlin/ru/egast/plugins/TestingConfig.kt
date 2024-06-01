package ru.egast.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import java.util.*

fun Application.connectToDb() {
    val dbProperties = Properties().apply {
        setProperty("user", environment.config.property("ktor.database.username").getString())
        setProperty("password", environment.config.property("ktor.database.password").getString())
        setProperty("ssl", "true")
        setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory")
        setProperty("port", environment.config.property("ktor.database.port").getString())
    }

    Database.connect(
        url = environment.config.property("ktor.database.jdbcUrl").getString(),
        driver = environment.config.property("ktor.database.driverClassName").getString(),
        setupConnection = { it.apply { clientInfo = dbProperties } })
}