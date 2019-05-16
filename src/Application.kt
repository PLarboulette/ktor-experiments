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
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import plarboulette.models.Hero
import plarboulette.models.PostHero
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

val heroes = mutableListOf<Hero>()

fun Routing.root() {
    get ("/") {
        call.respondText { "Welcome here" }
    }
    route("/heroes") {
        get {
            call.respond(
                mapOf("heroes" to synchronized(heroes) {
                    heroes.toList()
                })
            )
        }
        authenticate {
            post {
                val hero = call.receive<PostHero>()
                heroes += Hero(UUID.randomUUID(), hero.hero.name, hero.hero.age)
                call.respond(mapOf("OK" to true))
            }
        }

    }
}



