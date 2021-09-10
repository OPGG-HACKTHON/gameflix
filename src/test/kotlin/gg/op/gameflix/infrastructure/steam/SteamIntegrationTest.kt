package gg.op.gameflix.infrastructure.steam

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.Store.STEAM
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@Disabled
@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SteamConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
internal class SteamIntegrationTest {

    @Autowired
    private lateinit var configurationProperties: SteamConfigurationProperties

    private lateinit var steamClient: SteamClient
    private lateinit var steamService: SteamService

    @BeforeAll
    fun initializeInstance() {
        steamClient = SteamWebClient(configurationProperties)
        steamService = SteamService(steamClient)
    }

    @Test
    fun `when get store expect return steam`() {
        assertThat(steamService.store).isEqualTo(STEAM)
    }

    @Test
    fun `when application starts expect steam configuration properties initialized`() {
        assertThat(configurationProperties.baseUrl).isNotBlank
        assertThat(configurationProperties.apiKey).isBase64
    }

    @Test
    fun `when steamClient queryGetGames expect not empty`() {
        assertThat(steamClient.queryGetGames(SteamAuthentication("76561197960434622")).response.game_count).isPositive
    }

    @Test
    fun `when steamService getAllGameSlugsByAuthentication expect not empty`() {
        assertThat(steamService.getAllGameSlugsByAuthentication(SteamAuthentication("76561197960434622")))
            .containsOnlyOnce(GameSlug("Counter-Strike"))
    }
}