package no.webfluxoauth2.webfluxoauth2.oauth2.userdetails

class GithubOAuth2UserDetails(attributes: Map<String, Any>) : OAuth2UserDetails(attributes) {
    override val id: String
        get() = (attributes["id"] as Int).toString()
    override val name: String
        get() = attributes["name"] as String
    override val email: String
        get() = attributes["email"] as String
    override val imageUrl: String?
        get() = attributes["avatar_url"] as String?
}