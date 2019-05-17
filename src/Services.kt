package plarboulette

import plarboulette.models.Employee
import plarboulette.models.PostEmployee
import java.util.*

object Services {

    val employees = mutableListOf<Employee>()

    fun getEmployees (rank: String?) : Map<String, List<Employee>> {
        val list =  when (rank) {
            is String -> employees.toList().filter { it.rank.toString() == rank }
            else ->  employees.toList()
        }
        return  mapOf("employees" to synchronized(employees) { list })
    }

    fun getEmployee (id : UUID?) :Employee? {
        return employees.find { it.id == id }
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