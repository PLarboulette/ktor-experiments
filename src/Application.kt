package plarboulette

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
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

    routing {
        root()
    }

}

data class Hero (val name : String)

val heroes = Collections.synchronizedList(mutableListOf(
    Hero("Dark Vador"),
    Hero("Luke Skylwalker"))
)

fun Routing.root() {
    get ("/") {
        call.respondText { "OK here" }
    }
    get ("/json") {
        call.respond(mapOf("OK" to true))
    }
    get("/heroes") {
        call.respond(
            mapOf("heroes" to synchronized(heroes) {
                heroes.toList()
            })
        )
    }
    get("/heroes-filtered") {
        call.respond(
            mapOf("heroes-filtered" to synchronized(heroes) {
                heroes.filter { it.name.contains("Vador") }.toList()
            })
        )
    }
}



