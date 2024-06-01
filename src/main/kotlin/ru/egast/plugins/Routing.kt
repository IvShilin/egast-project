package ru.egast.plugins

import com.opencsv.CSVWriter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileWriter
import java.io.StringWriter
import java.sql.ResultSet

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        post("/v1/sql/custom") {
            val sqlQuery = call.receive<String>()
            val csvData = transaction {
                // Выполнение запроса и получение данных и метаданных
                val result = TransactionManager.current().exec(sqlQuery) { resultSet ->
                    resultSet.use { generateCsvFile(it) }
                }

                result ?: throw IllegalArgumentException("Failed to generate CSV")
            }

            // Отправка CSV клиенту
            call.respondFile(csvData)

            // Отправка CSV клиенту
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}

fun generateCsvFile(resultSet: ResultSet): File {
    // Создание временного файла
    val csvFile = File.createTempFile("export", ".csv")

    FileWriter(csvFile).use { writer ->
        val csvWriter = CSVWriter(writer)
        val metaData = resultSet.metaData
        val columnCount = metaData.columnCount

        // Запись заголовков
        val headers = (1..columnCount).map { metaData.getColumnName(it) }.toTypedArray()
        csvWriter.writeNext(headers, false)

        // Запись данных
        while (resultSet.next()) {
            val row = (1..columnCount).map { resultSet.getString(it) }.toTypedArray()
            csvWriter.writeNext(row, false)
        }

        csvWriter.close()
    }

    return csvFile
}