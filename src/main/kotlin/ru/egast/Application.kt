package ru.egast

import io.ktor.server.application.*
import io.ktor.server.netty.*
import ru.egast.plugins.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
//    configureDatabases()
    configureMonitoring()
    connectToDb()
    configureHTTP()
//    configureSecurity()
    configureRouting()
}
