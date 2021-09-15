package gg.op.gameflix.infrastructure.steam

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.Store.STEAM
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SteamConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
internal class SteamIntegrationTest {

    @Autowired
    private lateinit var configurationProperties: SteamConfigurationProperties

    private lateinit var steamClient: SteamClient

    private lateinit var mockSteamClient: SteamClient
    private lateinit var mockBackend: MockWebServer

    @BeforeAll
    fun initializeInstance() {
        steamClient = SteamWebClient(configurationProperties)

        mockBackend = MockWebServer()
        mockBackend.start()
        mockSteamClient = SteamConfigurationProperties("http://localhost:${mockBackend.port}", configurationProperties.apiKey)
            .let { SteamWebClient(it) }
    }

    @Test
    fun `when get store expect return steam`() {
        assertThat(SteamService(mockSteamClient).store).isEqualTo(STEAM)
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
        val steamService = SteamService(steamClient)
        assertThat(steamService.getAllGameSlugsByAuthentication(SteamAuthentication("76561199114515095")))
            .containsOnlyOnce(GameSlug("Portal"))
    }

    @Test
    fun `when steamBackend returns empty expect return empty`() {
        mockBackend.enqueue(
            MockResponse().setBody("{\"response\": {}}")
                .addHeader("Content-Type", "application/json")
        )
        val mockSteamService =  SteamService(mockSteamClient)

        assertThat(mockSteamService.getAllGameSlugsByAuthentication(SteamAuthentication(""))).isEmpty()
    }
}