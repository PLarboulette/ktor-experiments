package plarboulette.services

import plarboulette.models.Employee
import plarboulette.models.PostEmployee
import java.util.*
import arrow.core.*
import io.ktor.application.ApplicationCall
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.response.cacheControl
import io.ktor.response.respondTextWriter
import kotlinx.coroutines.channels.ReceiveChannel
import plarboulette.models.SseEvent

val employees = mutableListOf<Employee>()


fun getEmployees (rank: String?): List<Employee> {
    return when (rank) {
        is String -> employees.toList().filter { it.rank.toString() == rank }
        else ->  employees.toList()
    }
}


fun getEmployee (id : UUID?): Employee? {
    return employees.find { it.id == id }
}

fun createEmployee (postEmployee: PostEmployee): Employee {
    val employee = Employee(
        UUID.randomUUID(), postEmployee.employee.name,
        postEmployee.employee.salary, postEmployee.employee.rank)
    employees += employee
    return employee
}

object ScalaVersion {

    fun getEmployee (id : UUID?) : Option<Employee> {
        return Option.fromNullable(employees.find { it.id == id })
    }
}

fun wrapperError (message: String?): Map<String, Map<String, String?>> {
    return mapOf("errors" to mapOf("message" to message))
}

suspend fun ApplicationCall.respondSse(events: ReceiveChannel<SseEvent>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondTextWriter(contentType = ContentType.Text.EventStream) {
        for (event in events) {
            if (event.id != null) {
                write("id: ${event.id}\n")
            }
            if (event.event != null) {
                write("event: ${event.event}\n")
            }
            for (dataLine in event.data.lines()) {
                write("data: $dataLine\n")
            }
            write("\n")
            flush()
        }
    }
}