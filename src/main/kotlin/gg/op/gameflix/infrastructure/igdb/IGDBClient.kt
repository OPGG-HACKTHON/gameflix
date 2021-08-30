package gg.op.gameflix.infrastructure.igdb

import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils.getPage
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

sealed interface IGDBClient {
    fun queryGetGames(pageable: Pageable): Page<IGDBGame>
    fun queryGetCoverImages(ids: Collection<Int>): Set<IGDBCoverImage>
}

data class IGDBGame(
    val id: Int,
    val name: String,
    val cover: Int
)

@Suppress("kotlin:S117")
data class IGDBCoverImage(
    val id: Int,
    val image_id: String
) {
    companion object {
        private const val ID_INVALID = -1
        val NO_COVER_IMAGE = IGDBCoverImage(ID_INVALID, "nocover_qhhlj6")
    }

    fun toURI(): URI = URI.create("https://images.igdb.com/igdb/image/upload/t_cover_big/$image_id.jpg")
}


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
            .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
            .block()
            ?.let { igdbGameSummaries -> getPage(igdbGameSummaries, pageable, this::queryGetGamesCount) } ?: Page.empty()

    override fun queryGetCoverImages(ids: Collection<Int>) =
        webClient.post().uri("/covers")
            .bodyValue("fields id, image_id; where id = (${ids.joinToString { it.toString() }});")
            .retrieve()
            .bodyToMono(object: ParameterizedTypeReference<MutableList<IGDBCoverImage>>() {})
            .block()
            ?.toCollection(HashSet()) ?: emptySet()

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