package no.webfluxoauth2.webfluxoauth2.controller

import no.webfluxoauth2.webfluxoauth2.model.User
import no.webfluxoauth2.webfluxoauth2.model.UserPrincipal
import no.webfluxoauth2.webfluxoauth2.repository.UserRepository
import no.webfluxoauth2.webfluxoauth2.exception.ResourceNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @RequestMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    fun getCurrentUser(@AuthenticationPrincipal userPrincipal: UserPrincipal): User {
        return userRepository.findById(userPrincipal.id!!)
            .orElseThrow { ResourceNotFoundException("User", "id", userPrincipal.id) }
    }
}