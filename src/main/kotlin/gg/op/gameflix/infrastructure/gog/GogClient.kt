package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

sealed interface GogClient {
    fun queryGetGamesCode() : GogGamesCodeResponseDTO
    fun queryGetGames(getGames: GogGamesCodeResponseDTO) : ArrayList<GameSlug>
}

data class GogGamesCodeResponseDTO(
    val owned: List<Int>
)

private data class GogGamesResponseDTO(
    val title: String
)

class GogWebClient(properties: GogConfigurationProperties,authentication: GogAuthentication): GogClient {
    private val token = authentication.token
    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .defaultHeader(HttpHeaders.AUTHORIZATION, token)
        .build()

     override fun queryGetGamesCode():GogGamesCodeResponseDTO =
        webClient.get()
            .uri("/user/data/games")
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(GogGamesCodeResponseDTO::class.java)
            .block() ?: throw RuntimeException()

    override fun queryGetGames(getGames: GogGamesCodeResponseDTO):ArrayList<GameSlug> {
        val resultGames = ArrayList<GameSlug>()
        for(gameKey in getGames.owned){
            val gameName:GogGamesResponseDTO =
                webClient.get()
                    .uri("/account/gameDetails/${gameKey}.json")
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(GogGamesResponseDTO::class.java)
                    .block() ?: throw RuntimeException()
            resultGames.add(GameSlug(gameName.title))
        }
        return resultGames
    }

}