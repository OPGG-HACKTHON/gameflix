package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameSlug
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
class BlizzardClientTest {

    private lateinit var blizzardClient: BlizzardWebClient
    private lateinit var mockServer: ClientAndServer

    private val accessToken: String = "KRsu18Jdvy0QRntK4QqvwGgFj1cg2cGmyi"

    @BeforeAll
    fun initializeInstance() {
        mockServer = startClientAndServer(8090)
        blizzardClient = BlizzardWebClient(BlizzardConfigurationProperties("http://localhost:8090"))
    }

    @AfterAll
    fun stopServer() {
        mockServer.stop()
    }

    @Test
    fun `when blizzardClient queryGetGames expect not empty`() {
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withQueryStringParameter("access_token", accessToken)
                .withPath("/d3/data/act")
        ).respond(
            HttpResponse.response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\"acts\":[1, 2, 3]}")
                .withStatusCode(200)
        )
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withQueryStringParameter("access_token", accessToken)
                .withPath("/sc2/ladder/grandmaster/3")
        ).respond(
            HttpResponse.response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\"ladderTeams\":[1, 2, 3]}")
                .withStatusCode(200)
        )
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withQueryStringParameter("access_token", accessToken)
                .withQueryStringParameter("namespace", "profile-kr")
                .withPath("/profile/user/wow")
        ).respond(
            HttpResponse.response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{\"wow_accounts\":[1, 2, 3]}")
                .withStatusCode(200)
        )
        assertThat(blizzardClient.queryGetGames(BlizzardAuthentication(accessToken))).contains(GameSlug("Diablo III"))
    }
}