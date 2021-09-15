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
import kotlin.reflect.KClass
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
        private val GAME_REQUEST_BODY_BUILDER = IGDBRequestBodyBuilder(IGDBGame::class)
    }

    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .defaultHeader("Authorization", "Bearer ${properties.token}")
        .defaultHeader("Client-ID", properties.clientId)
        .defaultHeader("Accept", "application/json")
        .build()

    override fun queryGetGames(pageable: Pageable): Page<IGDBGame> =
        GAME_REQUEST_BODY_BUILDER.build(condition = "total_rating_count > 0", fieldToSort = "total_rating_count", pageable)
            .let { requestBody -> webClient.post().uri("/games")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
                .block()
                ?.let { igdbGameSummaries -> getPage(igdbGameSummaries, pageable) { queryGetGamesCount(requestBody) } }
                ?: Page.empty()
            }

    override fun queryGetGameBySlug(gameSlug: GameSlug): IGDBGame? =
        queryGetGamesBySlug(listOf(gameSlug))
            .firstOrNull()

    override fun queryGetGamesBySlug(gameSlugs: Collection<GameSlug>): Collection<IGDBGame> =
        GAME_REQUEST_BODY_BUILDER.build(condition = "slug = (${gameSlugs.joinToString(separator = ",") { "\"${it.slug}\"" }})")
            .let { requestBody -> webClient.post().uri("/games")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
                .block() ?: emptyList()
            }

    override fun queryGetGamesByName(name: String, pageable: Pageable): Page<IGDBGame> =
        GAME_REQUEST_BODY_BUILDER.build(valueToSearch = name, pageable = pageable)
            .let { requestBody -> webClient.post().uri("/games")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(object : ParameterizedTypeReference<MutableList<IGDBGame>>() {})
                    .block().orEmpty()
                    .let { igdbGameSummaries -> getPage(igdbGameSummaries, pageable) { queryGetGamesCount(requestBody) } }
            }

    override suspend fun queryGetCoverImages(ids: Collection<Int>): List<IGDBImage> =
        queryGetFindByIds("/covers", ids)

    override suspend fun queryGetGenres(ids: Collection<Int>): List<IGDBGenre> =
        queryGetFindByIds("/genres", ids)

    override suspend fun queryGetPlatforms(ids: Collection<Int>): List<IGDBPlatform> =
        queryGetFindByIds("/platforms", ids)

    override suspend fun queryGetDeveloperByInvolvedCompanies(ids: Collection<Int>): IGDBCompany? {
        data class IGDBInvolvedCompany(val id: Int, val company: Int, val developer: Boolean)

        return queryGetFindByIds<IGDBInvolvedCompany>("/involved_companies", ids)
            .find { it.developer }
            ?.let { queryGetFindByIds<IGDBCompany>("/companies", listOf(it.company)) }
            ?.firstOrNull()
    }

    override suspend fun queryGetScreenShots(ids: Collection<Int>): List<IGDBImage> =
        queryGetFindByIds("/screenshots", ids)

    private suspend inline fun <reified T : Any> queryGetFindByIds(uri: String, ids: Collection<Int>): List<T> =
        IGDBRequestBodyBuilder(T::class).buildFindByIds(ids)
            .let { requestBody -> webClient.post().uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .awaitBody<MutableList<T>>()
            }

    private fun queryGetGamesCount(requestBody: String): Long {
        data class CountDTO(val count: Long)
        return webClient.post().uri("/games/count")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(CountDTO::class.java)
            .block()
            ?.run { count } ?: 0
    }
}

data class IGDBRequestBodyBuilder<T: Any>(
    private val responseClass: KClass<T>,
) {
    private val fieldsCommaSeparated = responseClass.declaredMemberProperties.joinToString(separator = ",") { it.name }
    private val conditionsDefault = responseClass.declaredMemberProperties.joinToString(separator = "&") { "${it.name} != null" }

    fun buildFindByIds(ids: Collection<Int>, fieldToSort: String? = null, pageable: Pageable? = null, valueToSearch: String? = null): String =
        build(condition = "id = (${ids.joinToString(separator = ",") { it.toString() }})",
            fieldToSort = fieldToSort,
            pageable = pageable,
            valueToSearch = valueToSearch)

    fun build(condition: String? = null, fieldToSort: String? = null, pageable: Pageable? = null, valueToSearch: String? = null): String =
        buildString {
            append("fields $fieldsCommaSeparated;")
            append("where $conditionsDefault ${condition?.let { "& $it" } ?: ""};")

            if (fieldToSort != null) {
                append("sort $fieldToSort desc;")
            }
            if (pageable != null) {
                append(pageable.toIGDBPageableStatement())
            }
            if (valueToSearch != null) {
                append("search \"$valueToSearch\";")
            }
    }

    private fun Pageable.toIGDBPageableStatement(): String =
        "offset ${pageNumber * pageSize};" +
            "limit ${pageSize};"
}