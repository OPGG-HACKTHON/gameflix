package gg.op.gameflix.infrastructure.steam

import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient

sealed interface SteamClient {
    fun queryGetGames(authentication: SteamAuthentication): SteamResponseDTO
}

data class SteamResponseDTO(
    val response: SteamMultipleGamesResponseDTO
)

@Suppress("kotlin:S117")
data class SteamMultipleGamesResponseDTO(
    val game_count: Int?,
    val games: List<SteamGameResponseDTO>?
) {
    data class SteamGameResponseDTO(
        val appid: Int,
        val name: String
    )
}

class SteamWebClient(properties: SteamConfigurationProperties): SteamClient {

    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .build()
    private val apiKey = properties.apiKey

    override fun queryGetGames(authentication: SteamAuthentication) =
        webClient.get()
            .uri("/IPlayerService/GetOwnedGames/v0001/?key=${apiKey}&steamid=${authentication.steamId}&format=json&include_appinfo=true&include_played_free_games=true")
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(SteamResponseDTO::class.java)
            .block() ?: throw RuntimeException()
}