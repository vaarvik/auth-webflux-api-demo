package no.webfluxoauth2.webfluxoauth2.model.payload

data class AuthResponseDto(var accessToken: String) {
    var tokenType = "Bearer"
}
