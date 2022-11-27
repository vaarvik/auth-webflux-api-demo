package no.webfluxoauth2.webfluxoauth2.login.regular

import no.webfluxoauth2.webfluxoauth2.model.User
import no.webfluxoauth2.webfluxoauth2.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.server.ResponseStatusException
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class RegularLoginTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var context: ApplicationContext

    @MockBean
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    internal fun setup() {
        val user = User()
        user.id = 1
        user.name = "Local User"
        user.email = "local@mock.com"
        user.password = "localPw"
        user.password = passwordEncoder.encode(user.password)
        Mockito.`when`(userRepository.findByEmail("local@mock.com")).thenReturn(Optional.of(user))

//        webTestClient = WebTestClient
//            .bindToApplicationContext(context)
//            .apply(
//                springSecurity()
//            )
//            .configureClient()
//            .build()
    }

    @Test
    fun returnsTokenWithStatus200UponRequestWithValidCredentials() {
        webTestClient
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue("{\"email\": \"local@mock.com\", \"password\": \"localPw\"}"))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.accessToken")
            .exists()
            .jsonPath("$.tokenType")
            .exists()
    }

    @Test
    fun returnsStatus401UponRequestWithInvalidUsername() {
        webTestClient
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue("{\"email\": \"wrong@mock.com\", \"password\": \"localPw\"}"))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.message").exists()
            .consumeWith {
                println(it.responseBody.toString())
            }
    }

    @Test
    fun returnsStatus401UponRequestWithInvalidPassword() {
        val response = ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.")
        webTestClient
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue("{\"email\": \"local@mock.com\", \"password\": \"wrongpw\"}"))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.message").exists()
            .consumeWith {
                println(it.responseBody.toString())
            }
    }
}