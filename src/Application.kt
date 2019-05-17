package plarboulette

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.annotations.NotNull
import plarboulette.models.*
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

    install(ContentNegotiation) {
        jackson {enable(SerializationFeature.INDENT_OUTPUT)}
    }

    install(Authentication) {
        basic {
            realm = "heroes"
            validate { if (it.name == "user" && it.password == "password") UserIdPrincipal("user") else null }
        }
    }


    routing {
        root()
    }
}


fun Routing.root() {
    get ("/") {
        call.respondText { "Welcome here" }
    }
    route("/employees") {
        get {
            val list = Services.getEmployees(call.request.queryParameters["rank"])
            call.respond(list)
        }
        authenticate {
            post {
                val employee = call.receive<PostEmployee>()
                call.respond(mapOf("id" to Services.createEmployee(employee).id ))
            }
        }
    }
    route("/employees/{id}") {
        get {
             try {
                 val uuid = UUID.fromString(call.parameters["id"])
                when (val oEmployee = Services.getEmployee(uuid)) {
                    is Employee -> call.respond(HttpStatusCode.OK, mapOf("data" to oEmployee))
                    else -> call.respond(HttpStatusCode.NotFound, Services.wrapperError("Employee doesn't exist."))
                }
            } catch (e: NumberFormatException) {
                 call.respond(HttpStatusCode.InternalServerError, Services.wrapperError(e.message))
            }
        }
    }

    post("/jwt") {
        call.respondText { "Hello JWT" }
    }
}



