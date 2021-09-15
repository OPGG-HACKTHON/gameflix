package gg.op.gameflix.application.web.security

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.Store
import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserGameService
import gg.op.gameflix.domain.user.UserRepository
import io.mockk.every
import io.mockk.mockkClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.findByIdOrNull

@EnableConfigurationProperties(SecurityConfigurationProperties::class)
@TestConfiguration
class SecurityTestConfiguration {

    companion object {
        const val MOCK_USER_ID = "mock-user-id"
        const val MOCK_USER_EMAIL = "mock-user@email.com"
        val MOCK_USER_GAME_SLUG = GameSlug("League of Legends", "cover")
        val MOCK_USER_GAMES = mutableSetOf(GameSummary(slug = MOCK_USER_GAME_SLUG,
            cover = "cover",
            releaseAt = 0,
            developer = "Riot Games"),
            GameSummary(
            slug = GameSlug("Portal"),
            cover = "cover",
            releaseAt = 0,
            store = Store.STEAM,
            developer = ""
        ))
        val MOCK_USER = User(MOCK_USER_ID, MOCK_USER_EMAIL, MOCK_USER_GAMES)
    }

    @Bean
    fun bearerAuthenticationProvider(): BearerAuthenticationProvider = mockkClass(BearerAuthenticationProvider::class)

    @Bean
    fun userGameService(): UserGameService = mockkClass(UserGameService::class)

    @Bean
    fun userRepository(): UserRepository {
        val userRepositoryMock = mockkClass(UserRepository::class)
        every { userRepositoryMock.findByIdOrNull(MOCK_USER_ID) } returns MOCK_USER
        every { userRepositoryMock.findByIdOrNull(neq(MOCK_USER_ID)) } returns null
        return userRepositoryMock
    }
}