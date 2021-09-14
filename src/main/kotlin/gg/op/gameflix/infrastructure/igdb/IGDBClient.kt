package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.Genre
import gg.op.gameflix.domain.game.Platform
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils.getPage
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitEntityList
import org.springframework.web.reactive.function.client.awaitExchange
import kotlin.reflect.full.declaredMemberProperties

sealed interface IGDBClient {
    fun queryGetGames(pageable: Pageable): Page<IGDBGame>
    fun queryGetGameBySlug(gameSlug: GameSlug): IGDBGame?
    fun queryGetGamesBySlug(gameSlugs: Collection<GameSlug>): Collection<IGDBGame>
    fun queryGetGamesByName(name: String, pageable: Pageable): Page<IGDBGame>

    suspend fun queryGetCoverImages(ids: Collection<Int>): List<IGDBImage>
    suspend fun queryGetGenres(ids: Collection<Int>): List<IGDBGenre>
    suspend fun queryGetPlatforms(ids: Collection<Int>): List<IGDBPlatform>
    suspend fun queryGetDeveloperByInvolvedCompanies(ids: Collection<Int>): IGDBCompany?
    suspend fun queryGetScreenShots(ids: Collection<Int>): List<IGDBImage>
}

@Suppress("kotlin:S117")
data class IGDBGame(
    val id: Int,
    val name: String,
    val slug: String,
    val cover: Int,
    val first_release_date: Int,
    val updated_at: Int,
    val url: String,
    val summary: String,
    val total_rating: Float,
    val total_rating_count: Int,
    val genres: List<Int>,
    val platforms: List<Int>,
    val involved_companies: List<Int>,
    val screenshots: List<Int>
)

@Suppress("kotlin:S117")
data class IGDBImage(
    val id: Int,
    val image_id: String
) {
    companion object {
        private const val ID_INVALID = -1
        val NO_COVER_IMAGE = IGDBImage(ID_INVALID, "nocover_qhhlj6")
    }

    fun toCoverURI() ="https://images.igdb.com/igdb/image/upload/t_cover_big/$image_id.jpg"
    fun toBackgroundURI() = "https://images.igdb.com/igdb/image/upload/t_screenshot_huge/$image_id.jpg"
}

interface IGDBResource {
   val id: Int
   val slug: String
}

data class IGDBGenre(override val id: Int, override val slug: String) : IGDBResource {
    fun toGenre() = Genre(slug)
}

data class IGDBPlatform(override val id: Int, override val slug: String) : IGDBResource {
    fun toPlatform() = Platform(slug)
}


data class IGDBCompany(override val id: Int, override val slug: String): IGDBResource

class IGDBWebClient(properties: IGDBConfigurationProperties) : IGDBClient {

    companion object RequestBody {
        private val FIELDS_TO_RECEIVE = IGDBGame::class.declaredMemberProperties.joinToString { it.name }
        private val CONDITION_DEFAULT = IGDBGame::class.declaredMemberProperties.joinToString(separator = "&") { "${it.name} != null" } + "& total_rating_count > 0"
        private const val FIELD_TO_SORT = "total_rating_count desc"
    }

    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .defaultHeader("Authorization", "Bearer ${properties.token}")
        .defaultHeader("Client-ID", properties.clientId)
        .defaultHeader("Accept", "application/json")
        .build()

    override fun queryGetGames(pageable: Pageable): Page<IGDBGame> =
        webClient.post().uri("/games")
            .bodyValue("fields $FIELDS_TO_RECEIVE; where $CONDITION_DEFAULT; sort $FIELD_TO_SORT; ${pageable.toIGDBQueryStatement()}")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
            .block()
            ?.let { igdbGameSummaries -> getPage(igdbGameSummaries, pageable, this::queryGetGamesCount) } ?: Page.empty()

    override fun queryGetGameBySlug(gameSlug: GameSlug) =
        webClient.post().uri("/games")
            .bodyValue("fields $FIELDS_TO_RECEIVE; where $CONDITION_DEFAULT & slug = \"${gameSlug.slug}\";")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
            .block()
            ?.getOrNull(0)

    override fun queryGetGamesBySlug(gameSlugs: Collection<GameSlug>): Collection<IGDBGame> =
        webClient.post().uri("/games")
            .bodyValue("fields $FIELDS_TO_RECEIVE; where $CONDITION_DEFAULT & slug = (${gameSlugs.joinToString { "\"${it.slug}\"" }});")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
            .block() ?: emptyList()

    override fun queryGetGamesByName(name: String, pageable: Pageable) =
       webClient.post().uri("/games")
           .bodyValue("fields $FIELDS_TO_RECEIVE; where $CONDITION_DEFAULT; search \"$name\"; ${pageable.toIGDBQueryStatement()}")
           .retrieve()
           .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
           .block().orEmpty()
           .let { igdbGameSummaries -> getPage(igdbGameSummaries, pageable) { queryGetGamesByNameCount(name) } }

    override suspend fun queryGetCoverImages(ids: Collection<Int>): List<IGDBImage> =
        webClient.post().uri("/covers")
            .bodyValue("fields id, image_id; where id = (${ids.joinToString { it.toString() }});")
            .retrieve()
            .awaitBody<MutableList<IGDBImage>>()

    override suspend fun queryGetGenres(ids: Collection<Int>) =
        queryGetResources("/genres", ids)
            .map { IGDBGenre(it.id, it.slug) }

    override suspend fun queryGetPlatforms(ids: Collection<Int>) =
        queryGetResources("/platforms", ids)
            .map { IGDBPlatform(it.id, it.slug) }

    override suspend fun queryGetDeveloperByInvolvedCompanies(ids: Collection<Int>): IGDBCompany? {
        data class IGDBInvolvedCompany(val id: Int, val company: Int, val developer: Boolean)
        return webClient.post().uri("/involved_companies")
            .bodyValue("fields ${IGDBInvolvedCompany::class.declaredMemberProperties.joinToString { it.name }};" +
                "where id = (${ids.joinToString { it.toString() }});"
            )
            .retrieve()
            .awaitBody<MutableList<IGDBInvolvedCompany>>()
            .find { it.developer }
            ?.let { queryGetResources("/companies", listOf(it.company))
                .map { resource -> IGDBCompany(resource.id, resource.slug) } }
            ?.firstOrNull()
    }

    override suspend fun queryGetScreenShots(ids: Collection<Int>): List<IGDBImage> =
        webClient.post().uri("/screenshots")
            .bodyValue("fields id, image_id; where id = (${ids.joinToString { it.toString() }});")
            .retrieve()
            .awaitBody<MutableList<IGDBImage>>()

    private fun Pageable.toIGDBQueryStatement() = "offset ${pageNumber * pageSize}; limit $pageSize;"

    private suspend fun queryGetResources(uri: String, ids: Collection<Int>): List<IGDBResource> {
        data class IGDBResourceImpl(override val id: Int, override val slug: String) : IGDBResource

        return webClient.post().uri(uri)
            .bodyValue("fields ${IGDBResource::class.declaredMemberProperties.joinToString { it.name }};" +
                "where id = (${ids.joinToString { it.toString() }});")
            .retrieve()
            .awaitBody<MutableList<IGDBResourceImpl>>()
    }

    private fun queryGetGamesCount() =
        queryGetResultCounts("where $CONDITION_DEFAULT;")

    private fun queryGetGamesByNameCount(name: String) =
        queryGetResultCounts("where $CONDITION_DEFAULT; search \"$name\";")

    private fun queryGetResultCounts(requestBody: String): Long {
        data class CountDTO(val count: Long)

        return webClient.post().uri("/games/count")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(CountDTO::class.java)
            .block()
            ?.run { count } ?: 0
    }

}