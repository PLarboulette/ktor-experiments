package plarboulette

import io.ktor.http.HttpStatusCode
import plarboulette.models.Employee
import plarboulette.models.PostEmployee
import java.util.*

object Services {

    val employees = mutableListOf<Employee>()

    fun getEmployees (rank: String?) : Map<String, List<Employee>> {
        return when (rank) {
            is String ->
                mapOf("employees" to synchronized(employees) {
                    employees.toList().filter { it.rank.toString() == rank }
                })

            else ->
                mapOf("employees" to synchronized(employees) { employees.toList() }
                )
        }
    }

    fun getEmployee (id : UUID?) :Employee? {
        return when (val employee = employees.find { it.id == id }) {
            is Employee -> employee
            else -> null
        }
    }

    fun createEmployee (postEmployee: PostEmployee): Employee {
        val employee = Employee(
            UUID.randomUUID(), postEmployee.employee.name,
            postEmployee.employee.salary, postEmployee.employee.rank)
        employees += employee
        return employee
    }

    fun wrapperError (message: String?): Map<String, Map<String, String?>> {
        return mapOf("errors" to mapOf("message" to message))
    }


}