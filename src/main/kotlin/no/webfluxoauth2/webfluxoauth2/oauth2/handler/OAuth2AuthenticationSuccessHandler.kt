package no.webfluxoauth2.webfluxoauth2.oauth2.handler

import no.webfluxoauth2.webfluxoauth2.config.AppProperties
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils.addCookie
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils.getCookie
import no.webfluxoauth2.webfluxoauth2.token.JwtTokenProvider
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration
import kotlin.RuntimeException

@Component
class OAuth2AuthenticationSuccessHandler(private val appProperties: AppProperties, private val jwtTokenProvider: JwtTokenProvider): RedirectServerAuthenticationSuccessHandler() {
    private val REDIRECT_PARAM = appProperties.oauth2.redirectUriAfterLoginParam

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {
        val token = jwtTokenProvider.createToken(authentication)
        val paramRedirectUri = getCookie(webFilterExchange.exchange.request, REDIRECT_PARAM)
        val redirectUri = paramRedirectUri?.value ?: appProperties.oauth2.fallbackRedirectUri

        if(!isAuthorizedRedirectUri(redirectUri))
            throw RuntimeException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication.")

        setLocation(URI.create("${redirectUri}?token=${token}"))

        // Add token cookie
        addCookie(
            webFilterExchange.exchange.response,
            appProperties.auth.tokenCookie,
            token
        )

        return super.onAuthenticationSuccess(webFilterExchange, authentication)
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri = URI.create(uri)
        return appProperties.oauth2.authorizedRedirectUris
            .stream()
            .anyMatch {
                // Only validate host and port. Let the clients use different paths if they want to
                val authorizedURI = URI.create(it)
                if (authorizedURI.host.equals(clientRedirectUri.host, ignoreCase = true)
                    && authorizedURI.port == clientRedirectUri.port
                ) return@anyMatch true

                false
            }
    }

}