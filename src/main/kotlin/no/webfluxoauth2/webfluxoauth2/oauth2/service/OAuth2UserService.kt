package no.webfluxoauth2.webfluxoauth2.oauth2.service

import no.webfluxoauth2.webfluxoauth2.model.AuthProvider
import no.webfluxoauth2.webfluxoauth2.model.User
import no.webfluxoauth2.webfluxoauth2.model.UserPrincipal
import no.webfluxoauth2.webfluxoauth2.oauth2.exception.OAuth2AuthenticationProcessingException
import no.webfluxoauth2.webfluxoauth2.repository.UserRepository
import no.webfluxoauth2.webfluxoauth2.oauth2.userdetails.OAuth2UserDetails
import no.webfluxoauth2.webfluxoauth2.oauth2.userdetails.OAuth2UserDetailsFactory
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasLength
import reactor.core.publisher.Mono

@Service
class OAuth2UserService(private val userRepository: UserRepository): DefaultReactiveOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): Mono<OAuth2User> {
        val oAuth2ProviderSlug = userRequest.clientRegistration.registrationId
        val oAuth2User = super.loadUser(userRequest)
        return oAuth2User.flatMap {
            val customOAuth2User : OAuth2User = createCustomOAuthUser(it, oAuth2ProviderSlug)
            Mono.just(customOAuth2User)
        }
    }


    private fun createCustomOAuthUser(oAuth2User: OAuth2User, oAuth2ProviderSlug: String): OAuth2User {
        val oAuth2UserDetails = OAuth2UserDetailsFactory.getOAuth2UserInfo(oAuth2ProviderSlug, oAuth2User.attributes)

        try {
            lateinit var user: User

            if(!isValidOAuth2UserDetails(oAuth2UserDetails))
                throw OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider")

            val userOptional = userRepository.findByEmail(oAuth2UserDetails.email)

            if(userOptional.isPresent) {
                user = userOptional.get()
                if(userExistWithDifferentProvider(oAuth2ProviderSlug, user))
                    throw OAuth2AuthenticationProcessingException("You can't login with ${AuthProvider.valueOf(oAuth2ProviderSlug)} because you've already signed up with ${user.provider}.")

                updateExistingUser(user, oAuth2UserDetails)
            }
            else user = registerNewUser(oAuth2UserDetails, oAuth2ProviderSlug)

            return UserPrincipal.create(user, oAuth2User.attributes)

        } catch (ex: AuthenticationException) {
            throw ex
        } catch (ex: Exception) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw InternalAuthenticationServiceException(ex.message, ex.cause)
        }
    }

    private fun registerNewUser(oAuth2UserDetails: OAuth2UserDetails, oAuth2ProviderSlug: String): User {
        val user = User()
        val requestedProvider = AuthProvider.valueOf(oAuth2ProviderSlug)
        user.provider = requestedProvider
        user.providerId = oAuth2UserDetails.id
        user.name = oAuth2UserDetails.name
        user.email = oAuth2UserDetails.email
        user.imageUrl = oAuth2UserDetails.imageUrl
        return userRepository.save(user)
    }

    private fun updateExistingUser(existingUser: User, oAuth2UserDetails: OAuth2UserDetails): User {
        existingUser.name = oAuth2UserDetails.name
        existingUser.imageUrl = oAuth2UserDetails.imageUrl
        return userRepository.save(existingUser)
    }

    private fun isValidOAuth2UserDetails(oAuth2UserDetails: OAuth2UserDetails): Boolean {
        if(!hasLength(oAuth2UserDetails.email)) return false
        return true
    }

    private fun userExistWithDifferentProvider(oAuth2ProviderSlug: String, user: User): Boolean {
        val requestedProvider = AuthProvider.valueOf(oAuth2ProviderSlug)
        if (user.provider != requestedProvider) return true
        return false
    }
}