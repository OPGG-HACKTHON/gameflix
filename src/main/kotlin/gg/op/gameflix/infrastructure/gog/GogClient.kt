package gg.op.gameflix.infrastructure.gog

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient

sealed interface GogClient {
    fun queryGetGames(authentication: GogAuthentication): GogGamesResponseDTO
    fun queryGetGameDetails(authentication: GogAuthentication,titleKey: Int): GogGameDetailsResponseDTO
}

//게임 목록 DTO
data class GogGamesResponseDTO(
    val owned: List<Int>
)

//게임 상세 DTO
data class GogGameDetailsResponseDTO(
    val title: String
)

class GogWebClient(properties: GogConfigurationProperties,token:String): GogClient {


    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .defaultHeader(HttpHeaders.AUTHORIZATION, token)
        .build()


    //게임 목록 호출
    override fun queryGetGames(authentication: GogAuthentication):GogGamesResponseDTO =
        webClient.get()
            .uri("/user/data/games")
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(GogGamesResponseDTO::class.java)
            .block() ?: throw RuntimeException()

    //게임 상세 호출
    override fun queryGetGameDetails(authentication: GogAuthentication , titleKey:Int): GogGameDetailsResponseDTO =
        webClient.get()
            .uri("/account/gameDetails/${titleKey}.json")
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(GogGameDetailsResponseDTO::class.java)
            .block() ?: throw RuntimeException()
}