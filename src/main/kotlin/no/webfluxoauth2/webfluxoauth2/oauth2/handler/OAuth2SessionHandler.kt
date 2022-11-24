package no.webfluxoauth2.webfluxoauth2.oauth2.handler

import no.webfluxoauth2.webfluxoauth2.config.AppProperties
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils.addCookie
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils.deleteCookie
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository
import org.springframework.security.oauth2.client.web.server.WebSessionOAuth2ServerAuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class OAuth2SessionHandler(private val appProperties: AppProperties): ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private val sessionHandler = WebSessionOAuth2ServerAuthorizationRequestRepository()
    private val REDIRECT_PARAM = appProperties.oauth2.redirectUriAfterLoginParam

    override fun loadAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest> {
        return sessionHandler.loadAuthorizationRequest(exchange)
    }

    override fun removeAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest> {
        deleteCookie(exchange.request, exchange.response, REDIRECT_PARAM)
        return sessionHandler.removeAuthorizationRequest(exchange)
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest,
        exchange: ServerWebExchange
    ): Mono<Void> {
        val redirectUriAfterLogin = exchange.request.queryParams[REDIRECT_PARAM]

        if (!redirectUriAfterLogin.isNullOrEmpty())
            addCookie(
                exchange.response,
                REDIRECT_PARAM,
                redirectUriAfterLogin[0],
                Duration.ofMinutes(2)
            )

        return sessionHandler.saveAuthorizationRequest(authorizationRequest, exchange)
    }

}