package plarboulette

import plarboulette.models.Employee
import plarboulette.models.PostEmployee
import java.util.*
import arrow.*
import arrow.core.*

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
