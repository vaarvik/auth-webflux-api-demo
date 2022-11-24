package no.webfluxoauth2.webfluxoauth2.oauth2.userdetails

abstract class OAuth2UserDetails(var attributes: Map<String, Any>) {
    abstract val id: String
    abstract val name: String
    abstract val email: String
    abstract val imageUrl: String?
}