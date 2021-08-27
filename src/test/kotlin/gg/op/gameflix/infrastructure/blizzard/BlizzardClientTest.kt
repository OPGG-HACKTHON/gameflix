package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameSlug
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
class BlizzardClientTest {

    private lateinit var blizzardClient: BlizzardWebClient
    private lateinit var mockServer: ClientAndServer

    private val accessToken: String = "KRsu18Jdvy0QRntK4QqvwGgFj1cg2cGmyi"

    @BeforeEach
    fun initializeInstance() {
        mockServer = startClientAndServer(8080)
        blizzardClient = BlizzardWebClient(BlizzardConfigurationProperties("http://localhost:8080"))
    }

    @AfterEach
    fun stopServer() {
        mockServer.stop()
    }

    @Test
    fun `when blizzardClient queryGetGames expect not empty`() {
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/d3/data/act?access_token=${accessToken}")
        ).respond(
            HttpResponse.response()
                .withBody("{\"acts\":[1, 2, 3]}")
                .withStatusCode(200)
        )
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/sc2/ladder/grandmaster/3?access_token=${accessToken}")
        ).respond(
            HttpResponse.response()
                .withBody("{\"ladderTeams\":[1, 2, 3]}")
                .withStatusCode(200)
        )
        mockServer.`when`(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/profile/user/wow?namespace=profile-kr&access_token=${accessToken}")
        ).respond(
            HttpResponse.response()
                .withBody("{\"wow_accounts\":[1, 2, 3]}")
                .withStatusCode(200)
        )
        println(blizzardClient.queryGetGames(BlizzardAuthentication(accessToken)))
        println(BlizzardWebClient.WowInfoResponseDTO(wow_accounts = listOf("d3")).toString())
        assertThat(blizzardClient.queryGetGames(BlizzardAuthentication(accessToken))).contains(GameSlug("Diablo III"))
    }
}