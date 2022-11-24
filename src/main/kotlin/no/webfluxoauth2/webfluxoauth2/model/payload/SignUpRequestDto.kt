package no.webfluxoauth2.webfluxoauth2.model.payload

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class SignUpRequestDto(
    var name: @NotBlank String,
    var email: @NotBlank @Email String,
    var password: @NotBlank String
)
