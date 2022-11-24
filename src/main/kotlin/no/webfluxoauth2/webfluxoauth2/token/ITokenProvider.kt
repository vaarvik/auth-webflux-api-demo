package no.webfluxoauth2.webfluxoauth2.token

import no.webfluxoauth2.webfluxoauth2.model.User
import no.webfluxoauth2.webfluxoauth2.model.UserPrincipal
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
interface ITokenProvider {
    fun createToken(authentication: Authentication): String
    fun validateToken(token: String?): Boolean
}