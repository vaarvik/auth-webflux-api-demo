package no.webfluxoauth2.webfluxoauth2.oauth2.handler

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OAuth2AuthenticationFailureHandler: ServerAuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        webFilterExchange: WebFilterExchange,
        exception: AuthenticationException
    ): Mono<Void> {
        val failureHandler: ServerAuthenticationFailureHandler = RedirectServerAuthenticationFailureHandler("/failure!")
        return failureHandler.onAuthenticationFailure(webFilterExchange, exception)
    }

}