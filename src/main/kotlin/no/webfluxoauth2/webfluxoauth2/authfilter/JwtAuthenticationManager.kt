package no.webfluxoauth2.webfluxoauth2.authfilter

import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Authenticates the token that we handled in the converter
 * This class defines the @AuthenticationPrincipal that is used in for example the UserController
 */
@Service
class JwtAuthenticationManager : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.justOrEmpty(authentication)
            .filter{auth ->
                auth is UsernamePasswordAuthenticationToken
            }
            .cast(UsernamePasswordAuthenticationToken::class.java)
            .flatMap {
                mono {
                    UsernamePasswordAuthenticationToken(it.principal, it.credentials, it.authorities)
                }
            }
    }

}