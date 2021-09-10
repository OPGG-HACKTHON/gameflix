package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.Store.BLIZZARD
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(PER_CLASS)
@ExtendWith(MockKExtension::class)
internal class BlizzardServiceTest {

    private var configurationProperties: BlizzardConfigurationProperties = BlizzardConfigurationProperties("http://localhost:8080")

    @MockK
    private lateinit var blizzardClient: BlizzardClient
    private lateinit var blizzardService: BlizzardService

    private val accessToken: String = "KRsu18Jdvy0QRntK4QqvwGgFj1cg2cGmyi"

    @BeforeEach
    fun initializeInstance() {
        blizzardService = BlizzardService(blizzardClient)
    }

    @Test
    fun `when get store expect return blizzard`() {
        assertThat(blizzardService.store).isEqualTo(BLIZZARD)
    }

    @Test
    fun `when application starts expect blizzard configuration properties initialized`() {
        assertThat(configurationProperties.baseUrl).isNotBlank
    }

    @Test
    fun `when blizzardClient getAllGameSlugsByAuthentication expect not empty`() {
        val authentication = BlizzardAuthentication(accessToken)
        every { blizzardClient.queryGetGames(authentication) } returns listOf(GameSlug("wow"))

        assertThat(blizzardClient.queryGetGames(authentication)).contains(GameSlug("wow"))
    }
}