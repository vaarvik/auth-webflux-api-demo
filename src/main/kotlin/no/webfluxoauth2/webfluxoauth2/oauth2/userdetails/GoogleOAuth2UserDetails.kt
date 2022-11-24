package no.webfluxoauth2.webfluxoauth2.oauth2.userdetails

class GoogleOAuth2UserDetails(attributes: Map<String, Any>) : OAuth2UserDetails(attributes) {
    override val id: String
        get() = attributes["sub"] as String
    override val name: String
        get() = attributes["name"] as String
    override val email: String
        get() = attributes["email"] as String
    override val imageUrl: String?
        get() = attributes["picture"] as String?
}