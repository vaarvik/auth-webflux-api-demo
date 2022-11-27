package no.webfluxoauth2.webfluxoauth2.login.oauth2

import no.webfluxoauth2.webfluxoauth2.model.AuthProvider
import no.webfluxoauth2.webfluxoauth2.model.User
import no.webfluxoauth2.webfluxoauth2.oauth2.handler.OAuth2AuthenticationFailureHandler
import no.webfluxoauth2.webfluxoauth2.oauth2.handler.OAuth2AuthenticationSuccessHandler
import no.webfluxoauth2.webfluxoauth2.oauth2.handler.OAuth2SessionHandler
import no.webfluxoauth2.webfluxoauth2.oauth2.service.OAuth2UserService
import no.webfluxoauth2.webfluxoauth2.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*


//@WebFluxTest
//@ContextConfiguration(classes = [AppProperties::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension::class)
class OAuth2LoginTest {
//    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var oAuth2UserService: OAuth2UserService

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @MockBean
    lateinit var userRepository: UserRepository

    val googleOAuth2Client: SecurityMockServerConfigurers.OAuth2ClientMutator = mockOAuth2Client("google")

    val facebookOAuth2Client: SecurityMockServerConfigurers.OAuth2ClientMutator = mockOAuth2Client("facebook")

    @BeforeEach
    internal fun beforeAll() {
        val googleProvider = AuthProvider.valueOf("google")
        val googleUser = User()
        googleUser.id = 1
        googleUser.provider = googleProvider
        googleUser.providerId = "0"
        googleUser.name = "Google Mock"
        googleUser.email = "mock@gmail.com"
        googleUser.imageUrl = "https://picsum.photos/200/200"
        Mockito.`when`(userRepository.findByEmail("mock@gmail.com")).thenReturn(Optional.of(googleUser))

        val facebookProvider = AuthProvider.valueOf("facebook")
        val facebookUser = User()
        facebookUser.id = 1
        facebookUser.provider = facebookProvider
        facebookUser.providerId = "0"
        facebookUser.name = "Google Mock"
        facebookUser.email = "facebook@mock.com"
        facebookUser.imageUrl = "https://picsum.photos/200/200"
        Mockito.`when`(userRepository.findByEmail("facebook@mock.com")).thenReturn(Optional.of(facebookUser))

//        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build();
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
            // add Spring Security test Support
            .apply(springSecurity())
            .configureClient()
            .build()
    }

    @Test
    fun returnsTokenWithStatus200WithValidResponseFromGoogle() {
        val oauth2User: OAuth2User = DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("SCOPE_profile:read", "SCOPE_email:read"),
            mapOf(Pair("user_name", "foo_user")),
            "user_name"
        )

        println(googleOAuth2Client)
        val something = webTestClient
            .mutateWith(csrf())
//            .mutateWith(mockOAuth2Login())
//            .mutateWith(mockOAuth2Login().oauth2User(oauth2User))
            .mutateWith(mockOAuth2Client("google"))
//            .mutateWith(
//                mockOAuth2Login()
//                    .authorities(SimpleGrantedAuthority("SCOPE_email:read"), SimpleGrantedAuthority("SCOPE_profile:read"))
//            )
            .get()
                .uri("/oauth2/callback/google")
            .exchange()

        println(something.toString())
//        mockMvc.request(googleOAuth2Client.)
//            .perform(
//                post("/oauth2/callback/google").
//                .content(googleOAuth2Client.)
//                    .contentType(MediaType.APPLICATION_JSON)
//            )
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.accessToken").exists())
//            .andExpect(jsonPath("$.tokenType").exists())
    }
}

@Configuration
@ComponentScan
class TestConfig {
//    @Bean
//    fun webTestClient(applicationContext: ApplicationContext): WebTestClient {
//        return WebTestClient.bindToApplicationContext(applicationContext).build()
//    }

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange().anyExchange().permitAll().and().oauth2Login{
            it
                .authenticationMatcher(PathPatternParserServerWebExchangeMatcher("/oauth2/callback/{registrationId}"))
        }
        return http.build()
    }
}