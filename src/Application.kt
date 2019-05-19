package plarboulette

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.generateNonce
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import plarboulette.models.ChatSession
import plarboulette.models.InvalidCredentialsException
import plarboulette.models.SimpleJWT
import plarboulette.models.SseEvent
import plarboulette.services.ChatService
import java.time.Duration

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    val server = ChatService()

    val simpleJwt = SimpleJWT("my-super-secret") // put it in config

    val channel = produce { // this: ProducerScope<SseEvent> ->
        var n = 0
        while (true) {
            send(SseEvent("Server-side event $n"))
            delay(1000)
            n++
        }
    }.broadcast()

    install(ContentNegotiation) {
        jackson {enable(SerializationFeature.INDENT_OUTPUT)}
    }

    install(Authentication) {

        /*basic {
            realm = "employees"
            validate { if (it.name == "user" && it.password == "password") UserIdPrincipal("user") else null }
        }*/
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    install(StatusPages) {
        exception<InvalidCredentialsException> { exception ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost()
    }

    install(DefaultHeaders)

    install(CallLogging)

    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)
    }
    // This enables the use of sessions to keep information between requests/refreshes of the browser.
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

    // This adds an interceptor that will create a specific session in each request if no session is available already.
    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<ChatSession>() == null) {
            call.sessions.set(ChatSession(generateNonce()))
        }
    }

    routing {
        root(simpleJwt, channel, server)
    }
}

