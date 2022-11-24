package no.webfluxoauth2.webfluxoauth2.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app") // sets variables using attributes from "app" in application.yaml
class AppProperties {
    val auth = Auth()
    val oauth2 = OAuth2()
    val cors = Cors()

    class Auth {
        lateinit var tokenSecret: String
        var tokenExpirationMsec: Long = 0
        lateinit var tokenCookie: String
    }

    class OAuth2 {
        var authorizedRedirectUris: List<String> = ArrayList()
            private set
        lateinit var fallbackRedirectUri: String
        lateinit var redirectUriAfterLoginParam: String
    }

    class Cors {
        var allowedOrigins: List<String> = ArrayList()
            private set
    }
}