package no.webfluxoauth2.webfluxoauth2.controller

import no.webfluxoauth2.webfluxoauth2.model.User
import no.webfluxoauth2.webfluxoauth2.model.payload.ApiResponseDto
import no.webfluxoauth2.webfluxoauth2.model.payload.AuthResponseDto
import no.webfluxoauth2.webfluxoauth2.model.payload.LoginRequestDto
import no.webfluxoauth2.webfluxoauth2.model.payload.SignUpRequestDto
import no.webfluxoauth2.webfluxoauth2.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(val authService: AuthService) {
    @PostMapping("/login")
    fun authenticateUser(@RequestBody loginRequest: @Valid LoginRequestDto, response: ServerHttpResponse): ResponseEntity<*> {
        return try {
            val token = authService.handleLogin(
                    loginRequest.email,
                    loginRequest.password,
                    response
            )

            return ResponseEntity
                .ok(AuthResponseDto(token))
        } catch (error: ResponseStatusException) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto(false, "Invalid credentials."))
        } catch (error: RuntimeException) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto(false, error.message!!))
        }
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: @Valid SignUpRequestDto, request: ServerHttpRequest): ResponseEntity<*> {
        return try {
            val userInfoEndpoint = authService.handleRegistration(
                signUpRequest.name,
                signUpRequest.email,
                signUpRequest.password,
                request
            )

            ResponseEntity.created(userInfoEndpoint) // returns 201 with current user info endpoint in location in header
                    .body(ApiResponseDto(true, "User registered successfully."))
        } catch (error: RuntimeException) {
            ResponseEntity.badRequest().body(ApiResponseDto(false, error.message!!))
        }
    }

}
