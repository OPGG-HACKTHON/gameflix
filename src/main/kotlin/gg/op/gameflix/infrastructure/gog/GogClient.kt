package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient

sealed interface GogClient {
    fun queryGetGamesCode(gogAuthentication: GogAuthentication) : GogGamesCodeResponseDTO
    fun queryGetGames(getGames: GogGamesCodeResponseDTO, gogAuthentication: GogAuthentication) : ArrayList<GameSlug>
}

data class GogGamesCodeResponseDTO(
    val owned: List<Int>
)

private data class GogGamesResponseDTO(
    val title: String
)

class GogWebClient(properties: GogConfigurationProperties): GogClient {
    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .build()

     override fun queryGetGamesCode(gogAuthentication: GogAuthentication): GogGamesCodeResponseDTO =
        webClient.get()
            .uri("/user/data/games")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + gogAuthentication.token)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(GogGamesCodeResponseDTO::class.java)
            .block() ?: throw RuntimeException()

    override fun queryGetGames(getGames: GogGamesCodeResponseDTO, gogAuthentication: GogAuthentication): ArrayList<GameSlug> {
        val resultGames = ArrayList<GameSlug>()
        for(gameKey in getGames.owned){
            val gameName:GogGamesResponseDTO =
                webClient.get()
                    .uri("/account/gameDetails/${gameKey}.json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + gogAuthentication.token)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(GogGamesResponseDTO::class.java)
                    .block() ?: throw RuntimeException()
            resultGames.add(GameSlug(gameName.title))
        }
        return resultGames
    }

}