package no.webfluxoauth2.webfluxoauth2.authfilter

import no.webfluxoauth2.webfluxoauth2.token.JwtTokenProvider
import no.webfluxoauth2.webfluxoauth2.config.AppProperties
import no.webfluxoauth2.webfluxoauth2.model.UserPrincipal
import no.webfluxoauth2.webfluxoauth2.repository.UserRepository
import no.webfluxoauth2.webfluxoauth2.util.CookieUtils.getCookie
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasText
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Extracts the token and the jwt
 */
@Service
class JwtServerAuthenticationConverter(
    appProperties: AppProperties,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) : ServerAuthenticationConverter {

    private val tokenCookie = appProperties.auth.tokenCookie

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange)
            // Can maybe add filter here so this is not run unnecessarily when authentication is not needed.
            .filter{
                if(isOptionsAuthorizationRequest(it.request))
                    return@filter true

                var token = getTokenFromHeaderOrCookie(it.request)
                /**
                 * TODO: Istead make it so that it can't be headers when doing a GET or POST request...
                 * TODO: ...that does not need to have JSON. - For logout we can be specific though and let it through...
                 * TODO: ...and we can test if the token is attached on a request for logout becuase one is already logged in.
                 *
                 * Security improvement:
                 * If you keep the token expiry times at short enough intervals,
                 * and have the running client keep track and request updates when necessary,
                 * number 1 would effectively work as a complete logout system. The problem with this method,
                 * is that it makes it impossible to keep the user logged in between closes of the client code
                 * (depending on how long you make the expiry interval).
                 *
                 * When using JWT in this manner, there are a few specific properties:
                 * * The tokens are short-lived. They only need to be valid for a few minutes, to allow a client to initiate the download.
                 * * The token is only expected to be used once. The application server would issue a new token for every download,
                    so any one token is just used to request a file once, and then thrown away. There's no persistent state, at all.
                 * * The application server still uses sessions. It's just the download server that uses tokens to authorize individual downloads,
                    because it doesn't need persistent state.
                 */
                jwtTokenProvider.validateToken(token)
            }
            .map {
                if(isOptionsAuthorizationRequest(it.request))
                    return@map UsernamePasswordAuthenticationToken(null, null)

                var token = getTokenFromHeaderOrCookie(it.request)

                val userId = jwtTokenProvider.getUserIdFromToken(token)
                val user = userRepository.findById(userId).get()
                val userDetails = UserPrincipal.create(user)
                UsernamePasswordAuthenticationToken(userDetails, null)
            }
    }

    private fun isOptionsAuthorizationRequest(request: ServerHttpRequest) =
        (request.method === HttpMethod.OPTIONS
                && request.headers.accessControlRequestHeaders.contains("authorization")
                && request.headers.accessControlRequestHeaders.size == 1)

    private fun getTokenFromHeaderOrCookie(
        request: ServerHttpRequest,
    ): String? {
        val authHeader = request.headers["Authorization"]?.lastOrNull()
        val cookie = getCookie(request, tokenCookie)?.value
        var token: String? = ""

        if (hasText(authHeader) && authHeader !== null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7, authHeader.length)
        } else if (hasText(cookie))
            token = cookie
        return token
    }
}
