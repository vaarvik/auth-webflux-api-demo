package no.webfluxoauth2.webfluxoauth2.service

import no.webfluxoauth2.webfluxoauth2.config.AppProperties
import no.webfluxoauth2.webfluxoauth2.exception.ResourceNotFoundException
import no.webfluxoauth2.webfluxoauth2.model.AuthProvider
import no.webfluxoauth2.webfluxoauth2.model.User
import no.webfluxoauth2.webfluxoauth2.model.UserPrincipal
import no.webfluxoauth2.webfluxoauth2.repository.UserRepository
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils.addCookie
import no.webfluxoauth2.webfluxoauth2.token.JwtTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Service
class AuthService(
    val tokenProvider: JwtTokenProvider,
    val appProperties: AppProperties,
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
) {
    fun handleLogin(email: @NotBlank @Email String, password: @NotBlank String, response: ServerHttpResponse) : String {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.") }

        user.let {
            if(passwordEncoder.matches(password, it.password)){
                val userDetails = UserPrincipal.create(user)
                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                val token = tokenProvider.createToken(authentication)
                addCookie(response, appProperties.auth.tokenCookie, token)
                return token
            }
        }

        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.")
    }

    fun handleRegistration(name: @NotBlank String, email: @NotBlank @Email String, password: @NotBlank String, request: ServerHttpRequest): URI {
        if (userRepository.existsByEmail(email))
            throw RuntimeException("Email address already in use.")

        val user = createUser(name, email, password)
        val registeredUser = userRepository.save(user)

        return getUserInfoUri(registeredUser, request)
    }

    private fun getUserInfoUri(user: User, request: ServerHttpRequest): URI {
        val backendUri = "${request.uri.scheme}${request.uri.host}"
        return UriComponentsBuilder
            .fromUriString(backendUri)
            .path("/user/me")
            .buildAndExpand(user.id)
            .toUri()
    }

    private fun createUser(name : String, email : String, password : String): User {
        val user = User()
        user.name = name
        user.email = email
        user.password = password
        user.provider = AuthProvider.local
        user.password = passwordEncoder.encode(user.password)
        return user
    }
}
