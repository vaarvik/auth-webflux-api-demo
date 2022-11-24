package no.webfluxoauth2.webfluxoauth2.config

import no.webfluxoauth2.webfluxoauth2.handler.LogoutHandler
import no.webfluxoauth2.webfluxoauth2.oauth2.handler.OAuth2SessionHandler
import no.webfluxoauth2.webfluxoauth2.oauth2.handler.OAuth2AuthenticationFailureHandler
import no.webfluxoauth2.webfluxoauth2.oauth2.handler.OAuth2AuthenticationSuccessHandler
import no.webfluxoauth2.webfluxoauth2.authfilter.JwtAuthenticationManager
import no.webfluxoauth2.webfluxoauth2.authfilter.JwtServerAuthenticationConverter
import no.webfluxoauth2.webfluxoauth2.token.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebFluxSecurity
class SecurityConfig(private val appProperties: AppProperties,
                     private val jwtTokenProvider: JwtTokenProvider,
                     private val jwtAuthenticationManager: JwtAuthenticationManager,
                     private val jwtServerAuthenticationConverter: JwtServerAuthenticationConverter,
                     private val logoutHandler: LogoutHandler
                     ) {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // Used by spring security if CORS is enabled.
    @Bean
    fun corsFilter(): CorsWebFilter {
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.allowedOrigins = appProperties.cors.allowedOrigins
        config.addAllowedHeader("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return CorsWebFilter(source)
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationFilter = AuthenticationWebFilter(jwtAuthenticationManager)
        authenticationFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter)

        http
            .cors()
                .and()
            .csrf()
                .disable()
            .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange {
                it
                    .pathMatchers(
                        "/auth/login",
                        "/auth/signup",
                    )
                    .permitAll()
                        .anyExchange()
                            .authenticated()
            }
            .logout {
                it.logoutUrl("/auth/logout")
                it.logoutHandler(logoutHandler)
            }
            .oauth2Login{
                it
                    .authenticationMatcher(PathPatternParserServerWebExchangeMatcher("/oauth2/callback/{registrationId}"))
                    .authenticationSuccessHandler(OAuth2AuthenticationSuccessHandler(appProperties, jwtTokenProvider))
                    .authenticationFailureHandler(OAuth2AuthenticationFailureHandler())
                    .authorizationRequestRepository(OAuth2SessionHandler(appProperties))
            }
            .exceptionHandling{
                it
                    .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.NOT_FOUND)) // Disable login entry point in backend
            }

        return http.build()
    }
}