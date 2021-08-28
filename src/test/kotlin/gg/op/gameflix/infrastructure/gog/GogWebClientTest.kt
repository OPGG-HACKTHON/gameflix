package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [GogConfiguration::class],
    initializers = [ConfigDataApplicationContextInitializer::class]
)
internal class GogWebClientTest {
    private lateinit var gogClientGameList: GogClient
    private lateinit var gogClientGames: GogClient
    private lateinit var mockBackEndGameList: MockWebServer
    private lateinit var mockBackEndGames: MockWebServer

    @BeforeAll
    fun startMockwebserver() {
        mockBackEndGameList = MockWebServer()
        mockBackEndGameList.start()
        mockBackEndGames = MockWebServer()
        mockBackEndGames.start()

    }

    @BeforeEach
    fun initializeGogClient(){
        val baseUrlGameList: String = "http://localhost:"+mockBackEndGameList.port
        val baseUrlGames: String = "http://localhost:"+mockBackEndGames.port
        var configurationPropertiesGameList: GogConfigurationProperties = GogConfigurationProperties(baseUrlGameList)
        var configurationPropertiesGames: GogConfigurationProperties = GogConfigurationProperties(baseUrlGames)
        var token =""

        gogClientGameList = GogWebClient(configurationPropertiesGameList, GogAuthentication("Bearer " + token))
        gogClientGames = GogWebClient(configurationPropertiesGames, GogAuthentication("Bearer " + token))

        mockBackEndGameList.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"owned\": [ 2078420771 ] }")
        )
        mockBackEndGames.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"title\": \"DISTRAINT 2\"}")
        )
    }

    @AfterAll
    fun shutdownMockwebserver(){
        mockBackEndGameList.shutdown()
        mockBackEndGames.shutdown()
    }

    @Test
    fun `when queryGetGameList expect not empty`() {
        assertThat(gogClientGameList.queryGetGamesCode()).isNotNull
    }

    @Test
    fun `when queryGetGames expect not empty`() {
        assertThat(gogClientGames.queryGetGames(gogClientGameList.queryGetGamesCode())).isNotNull
    }
}