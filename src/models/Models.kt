package plarboulette.models

import java.util.*

enum class Rank {
    DEVELOPER,
    ARCHITECT,
    MANAGER,
    DESIGNER,
    VP
}

data class Employee (val id: UUID, val name: String, val salary: Int, val rank: Rank)

// Input types

data class EmployeeInput (val name: String, val salary: Int, val rank: Rank)

data class PostEmployee (val employee: EmployeeInput)


// --------------------------------------------
class User(val name: String, val password: String)

val users: MutableMap<String, User> = Collections.synchronizedMap(
    listOf(User("test", "test"))
        .associateBy { it.name }
        .toMutableMap()
)

class LoginRegister(val user: String, val password: String)

// --------------------------------------------
class InvalidCredentialsException(message: String) : RuntimeException(message)

data class SseEvent(val data: String, val event: String? = null, val id: String? = null)
