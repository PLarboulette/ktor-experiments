package plarboulette

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Try
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import plarboulette.models.*
import java.util.*

fun Routing.root() {

    get ("/") {
        call.respondText { "Welcome here" }
    }

    post("/login-jwt") {
        val post = call.receive<LoginRegister>()
        val user = users.getOrPut(post.user) { User(post.user, post.password) }
        if (user.password != post.password) error("Invalid credentials")
        call.respond(mapOf("token" to simpleJwt.sign(user.name)))
    }

    route("/employees") {
        get {
            val list = getEmployees(call.request.queryParameters["rank"])
            call.respond(mapOf("employees" to list))
        }
        authenticate {
            post {
                val employee = call.receive<PostEmployee>()
                call.respond(mapOf("id" to createEmployee(employee).id ))
            }
        }
    }

    route("/employees/{id}") {
        get {

            // Kotlin version
            try {
                val uuid = UUID.fromString(call.parameters["id"])
                when (val oEmployee: Employee? = getEmployee(uuid)) {
                    is Employee -> call.respond(HttpStatusCode.OK, mapOf("data" to oEmployee))
                    else -> call.respond(HttpStatusCode.NotFound, wrapperError("Employee doesn't exist."))
                }
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.InternalServerError, wrapperError(e.message))
            }

            // Scala syntax
            Try {
                val uuid = UUID.fromString(call.parameters["id"])
                when (val oEmployee: Option<Employee> = ScalaVersion.getEmployee(uuid)) {
                    is Some -> call.respond(HttpStatusCode.OK, mapOf("data" to oEmployee.t))
                    is None -> call.respond(HttpStatusCode.NotFound, wrapperError("Employee doesn't exist."))
                }
            }.toEither().mapLeft { e ->
                call.respond(HttpStatusCode.InternalServerError, wrapperError(e.message))
            }
        }
    }

    route("/me") {
        authenticate {
            get {
                // Let to get employee information about the connected user
                val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                when(val list = getEmployees(call.request.queryParameters["rank"])
                    .find { it.name == principal.name }) {
                    is Employee -> call.respond(HttpStatusCode.OK, mapOf("employee" to list))
                    else -> call.respond(HttpStatusCode.NotFound)
                }

            }
        }
    }
}