package no.webfluxoauth2.webfluxoauth2.model.payload

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class LoginRequestDto(
        var email: @NotBlank @Email String,
        var password: @NotBlank String
)
