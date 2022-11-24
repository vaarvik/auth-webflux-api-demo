package no.webfluxoauth2.webfluxoauth2.handler

import no.webfluxoauth2.webfluxoauth2.config.AppProperties
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LogoutHandler(private val appProperties: AppProperties) : SecurityContextServerLogoutHandler() {
    override fun logout(exchange: WebFilterExchange, authentication: Authentication): Mono<Void> {
        CookieUtils.deleteCookie(exchange.exchange.request, exchange.exchange.response, appProperties.auth.tokenCookie)
        return super.logout(exchange, authentication)
    }
}