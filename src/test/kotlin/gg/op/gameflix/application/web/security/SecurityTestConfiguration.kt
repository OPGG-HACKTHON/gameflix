package gg.op.gameflix.application.web.security

import gg.op.gameflix.domain.user.UserGameService
import io.mockk.mockkClass
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class SecurityTestConfiguration {

    @Bean
    fun bearerAuthenticationProvider(): BearerAuthenticationProvider = mockkClass(BearerAuthenticationProvider::class)

    @Bean
    fun userGameService(): UserGameService = mockkClass(UserGameService::class)
}