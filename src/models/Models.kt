package plarboulette.models

import java.util.*

enum class Rank {
    DEVELOPER,
    ARCHITECT,
    MANAGER,
    DESIGNER,
    VP
}

data class Employee (val id: UUID, val same: String, val salary: Int, val rank: Rank)

// Input types

data class EmployeeInput (val name: String, val salary: Int, val rank: Rank)

data class PostEmployee (val employee: EmployeeInput)