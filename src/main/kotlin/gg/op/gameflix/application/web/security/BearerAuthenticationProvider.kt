package gg.op.gameflix.application.web.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class BearerAuthenticationProvider(
    private val googleIdTokenVerifier: GoogleIdTokenVerifier,
    private val userRepository: UserRepository
): AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication =
        googleIdTokenVerifier.verifyAuthentication(authentication)
            .run { userRepository.findByIdOrException(payload.subject) }
            .let { user -> UserAuthenticationToken(user) }

    override fun supports(authentication: Class<*>) =
        authentication.isAssignableFrom(BearerAuthenticationToken::class.java)

    private fun GoogleIdTokenVerifier.verifyAuthentication(authentication: Authentication) =
        verify(authentication.principal.toString())?: throw BadCredentialsException("Invalid Google ID Token")

    private fun UserRepository.findByIdOrException(id: String) =
        findByIdOrNull(id) ?: throw BadCredentialsException("User not exists")
}

class UserAuthenticationToken(
    private val user: User
): AbstractAuthenticationToken(mutableListOf(SimpleGrantedAuthority("USER"))) {

    override fun getPrincipal() = user

    override fun getCredentials() = null
}