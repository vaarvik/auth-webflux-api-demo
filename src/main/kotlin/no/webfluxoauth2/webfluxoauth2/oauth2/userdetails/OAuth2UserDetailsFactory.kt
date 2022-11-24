package no.webfluxoauth2.webfluxoauth2.oauth2.userdetails

import no.webfluxoauth2.webfluxoauth2.model.AuthProvider
import javax.naming.AuthenticationException
import kotlin.jvm.internal.Reflection

object OAuth2UserDetailsFactory {
    fun getOAuth2UserInfo(oAuth2ProviderSlug: String, attributes: Map<String, Any>): OAuth2UserDetails {
        val reflections = Reflection.typeOf(OAuth2UserDetails::class)
        return if (oAuth2ProviderSlug.equals(AuthProvider.google.toString(),ignoreCase = true)) {
            GoogleOAuth2UserDetails(attributes)
        } else if (oAuth2ProviderSlug.equals(AuthProvider.facebook.toString(), ignoreCase = true)) {
            FacebookOAuth2UserDetails(attributes)
        } else if (oAuth2ProviderSlug.equals(AuthProvider.github.toString(), ignoreCase = true)) {
            GithubOAuth2UserDetails(attributes)
        } else {
            throw AuthenticationException("Sorry! Login with $oAuth2ProviderSlug is not supported yet.")
        }
    }
}