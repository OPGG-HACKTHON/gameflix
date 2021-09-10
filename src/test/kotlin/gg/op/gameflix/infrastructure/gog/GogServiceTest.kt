package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.Store.GOG
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GogServiceTest {

    private lateinit var gogService: GogService
    private val mockWebServer = MockWebServer()

    @BeforeAll
    fun setUpMockWebServer() {
        mockWebServer.start()
        gogService = GogConfigurationProperties("http://localhost:${mockWebServer.port}")
            .let { configuration -> GogWebClient(configuration, GogAuthentication("Bearer token")) }
            .let { gogClient -> GogService(gogClient) }
    }

    @AfterAll
    fun tearDownMockWebServer(){
        mockWebServer.shutdown()
    }

    @Test
    fun `when get store expect return gog`() {
        assertThat(gogService.store).isEqualTo(GOG)
    }

    @Test
    fun `when getAllGameSlugsByAuthentication expect not empty`() {
        mockWebServer.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"owned\": [ 2078420771 ] }")
        )
        mockWebServer.enqueue(
            MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"title\": \"DISTRAINT 2\"}")
        )
        assertThat(gogService.getAllGameSlugsByAuthentication(GogAuthentication("Bearer token")))
             .containsOnlyOnce(GameSlug("DISTRAINT 2"))
    }
}
