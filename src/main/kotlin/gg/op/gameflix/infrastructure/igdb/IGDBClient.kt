package gg.op.gameflix.infrastructure.igdb

import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils.getPage
import org.springframework.web.reactive.function.client.WebClient

sealed interface IGDBClient {
    fun queryGetGames(pageable: Pageable): Page<IGDBGameSummary>
    fun queryGetCoverImages(coverIds: Collection<Int>): Map<Int, IGDBCoverImage>
}

data class IGDBGameSummary(
    val id: Int,
    val name: String,
    val cover: Int
)

data class IGDBCoverImage(
    private val imageId: String
)

class IGDBWebClient(properties: IGDBConfigurationProperties) : IGDBClient {

    companion object RequestBody {
        private const val FIELDS_TO_RECEIVE = "id, name, cover"
        private const val CONDITIONS_TO_QUERY = "cover != null & total_rating_count > 0 & genres != null & platforms != null"
        private const val OPTIONS_FOR_SORT = "sort total_rating_count desc"
    }

    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .defaultHeader("Authorization", "Bearer ${properties.token}")
        .defaultHeader("Client-ID", properties.clientId)
        .defaultHeader("Accept", "application/json")
        .build()

    override fun queryGetGames(pageable: Pageable) =
        webClient.post().uri("/games")
            .bodyValue("fields $FIELDS_TO_RECEIVE; where $CONDITIONS_TO_QUERY; $OPTIONS_FOR_SORT; ${pageable.toIGDBQueryStatement()}")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGameSummary>>() {})
            .block()
            ?.let { igdbGameSummaries -> getPage(igdbGameSummaries, pageable, this::queryGetGamesCount) } ?: Page.empty()

    override fun queryGetCoverImages(coverIds: Collection<Int>) =
        webClient.post().uri("/covers")
            .bodyValue("fields id, image_id; where id = (${coverIds.joinToString { it.toString() }});")
            .retrieve()
            .bodyToMono(object: ParameterizedTypeReference<MutableList<IGDBCoverImageResponseDTO>>() {})
            .block()
            ?.associate { it.id to IGDBCoverImage(it.image_id)} ?: emptyMap()

    private fun Pageable.toIGDBQueryStatement() = "offset $pageNumber; limit $pageSize;"

    private fun queryGetGamesCount() =
        webClient.post().uri("/games/count")
            .bodyValue("where $CONDITIONS_TO_QUERY;")
            .retrieve()
            .bodyToMono(CountDTO::class.java)
            .block()
            ?.run { count } ?: 0

    private data class CountDTO(val count: Long)

    @Suppress("kotlin:S117")
    private data class IGDBCoverImageResponseDTO(val id: Int, val image_id: String)

}