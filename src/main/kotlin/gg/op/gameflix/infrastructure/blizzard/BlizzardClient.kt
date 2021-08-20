package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameSlug
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.lang.RuntimeException

sealed interface BlizzardClient {
    fun queryGetGames(authentication: BlizzardAuthentication): Collection<GameSlug>
}

class BlizzardWebClient(properties: BlizzardConfigurationProperties): BlizzardClient {

    override fun queryGetGames(authentication: BlizzardAuthentication): MutableList<GameSlug> {
        val accessToken: String = authentication.accessToken
        val result = mutableListOf<GameSlug>()
        getD3Info(accessToken)
            ?.let { result.add(GameSlug("Diablo III")) }
        getSc2Info(accessToken)
            .let { result.add(GameSlug("StarCraft-II-wings-of-liberty")) }
        getWowInfo(accessToken)
            ?.let { result.add(GameSlug("wow")) }
        return result
    }

    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .build()

    data class D3InfoResponseDTO(
        val acts: List<Any>?
    )
    private fun getD3Info(accessToken: String) =
        webClient.get()
            .uri("/d3/data/act?access_token=${accessToken}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(D3InfoResponseDTO::class.java)
            .block()

    data class Sc2InfoResponseDTO(
        val ladderTeams: List<Any>?
    )
    private fun getSc2Info(accessToken: String) =
        webClient.get()
            .uri("/sc2/ladder/grandmaster/3?access_token=${accessToken}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({
                status -> status.is4xxClientError || status.is5xxServerError }, {
                    clientResponse ->
                        clientResponse.bodyToMono(String::class.java)
                            .map { body -> RuntimeException(body) }
                })
            .bodyToMono(Sc2InfoResponseDTO::class.java)
            .block()

    data class WowInfoResponseDTO(
        val wow_accounts: List<Any>?
    )
    private fun getWowInfo(accessToken: String) =
        webClient.get()
            .uri("/profile/user/wow?namespace=profile-kr&access_token=${accessToken}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({
                status -> status.is4xxClientError || status.is5xxServerError }, {
                    clientResponse ->
                        clientResponse.bodyToMono(String::class.java)
                        .map { body -> RuntimeException(body) }
            })
            .bodyToMono(WowInfoResponseDTO::class.java)
            .block()
}
