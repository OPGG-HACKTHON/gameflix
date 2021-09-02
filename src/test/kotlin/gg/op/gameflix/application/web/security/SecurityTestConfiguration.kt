package gg.op.gameflix.application.web.security

import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class SecurityTestConfiguration {

    @Bean
    fun bearerAuthenticationProvider(): BearerAuthenticationProvider = mock(BearerAuthenticationProvider::class.java)
}