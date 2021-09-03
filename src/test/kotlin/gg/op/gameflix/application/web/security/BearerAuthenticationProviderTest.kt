package gg.op.gameflix.application.web.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import gg.op.gameflix.domain.user.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication

@ExtendWith(MockKExtension::class)
internal class BearerAuthenticationProviderTest {

    private lateinit var bearerAuthenticationProvider: BearerAuthenticationProvider

    @MockK
    private lateinit var googleIdTokenVerifier: GoogleIdTokenVerifier
    @MockK
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun initialize_bearerAuthenticationProvider() {
        bearerAuthenticationProvider = BearerAuthenticationProvider(googleIdTokenVerifier, userRepository)
    }

    @Test
    fun `BearerAuthenticationProvider supports BearerAuthenticationToken`() {
        assertThat(bearerAuthenticationProvider.supports(BearerAuthenticationToken::class.java)).isTrue
    }

    @Test
    fun `when authentication invalid expect BadCredentialException`(@MockK authentication: Authentication) {
        whenAuthenticationTokenInvalid(authentication)

        assertThatThrownBy { bearerAuthenticationProvider.authenticate(authentication) }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessageStartingWith("Invalid Google ID Token")
    }

    private fun whenAuthenticationTokenInvalid(authentication: Authentication) {
        every { authentication.principal } answers {"INVALID_TOKEN"}
        every { googleIdTokenVerifier.verify("INVALID_TOKEN") } returns null
    }
}