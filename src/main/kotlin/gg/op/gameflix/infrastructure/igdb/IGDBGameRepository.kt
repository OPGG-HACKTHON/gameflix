package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameDetail
import gg.op.gameflix.domain.game.GameRating
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.infrastructure.igdb.IGDBImage.Companion.NO_COVER_IMAGE
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils.getPage

class IGDBGameRepository(private val igdbClient: IGDBClient) : GameRepository {

    override fun findAllGameSummaries(pageable: Pageable): Page<GameSummary> =
        igdbClient.queryGetGames(pageable)
            .toGameSummaries()

    override fun findFirstGameBySlug(slug: GameSlug): Game? =
        igdbClient.queryGetGameBySlug(slug)
            ?.toGame()

    override fun findAllGameSummariesByName(name: String, pageable: Pageable): Page<GameSummary> =
        igdbClient.queryGetGamesByName(name, pageable)
            .toGameSummaries()

    override fun findAllGameSummariesBySlugs(slugs: Collection<GameSlug>): Collection<GameSummary> =
        igdbClient.queryGetGamesBySlug(slugs)
            .toGameSummaries()

    private fun Collection<IGDBGame>.toGameSummaries(): List<GameSummary> =
        runBlocking {
            val gameSummaries = map { async { it.toGameSummary() } }
            gameSummaries.awaitAll()
        }

    suspend fun IGDBGame.toGameSummary(): GameSummary =
        coroutineScope {
            val coverImage = async { igdbClient.queryGetCoverImages(listOf(cover)).firstOrNull() }
            val developer = async { igdbClient.queryGetDeveloperByInvolvedCompanies(involved_companies) }

            GameSummary(slug = GameSlug(name),
                releaseAt = first_release_date,
                cover = coverImage.await()?.toCoverURI() ?: NO_COVER_IMAGE.toCoverURI(),
                developer = developer.await().firstOrNull()?.slug ?: "NOT Found")
        }

    private fun Page<IGDBGame>.toGameSummaries(): Page<GameSummary> {
        if (isEmpty) {
            return Page.empty()
        }
        return getPage(content.toGameSummaries(), pageable ) { totalElements }
    }

    private fun IGDBGame.toGame(): Game = runBlocking {
        val image = async { igdbClient.queryGetCoverImages(listOf(cover)).firstOrNull()?.toCoverURI() ?: NO_COVER_IMAGE.toCoverURI()}
        val genres = async { igdbClient.queryGetGenres(genres).map { it.toGenre() }.toHashSet() }
        val platforms = async { igdbClient.queryGetPlatforms(platforms).map { it.toPlatform() }.toHashSet() }
        val developer = async { igdbClient.queryGetDeveloperByInvolvedCompanies(involved_companies).firstOrNull()?.slug ?: "NOT FOUND" }
        val background = async { igdbClient.queryGetScreenShots(screenshots).firstOrNull()?.toBackgroundURI() ?: NO_COVER_IMAGE.toBackgroundURI() }

        runBlocking {
            Game(
                GameSummary(GameSlug(name), image.await(), first_release_date, developer = developer.await()),
                GameDetail(
                    genres = genres.await(),
                    platforms = platforms.await(),
                    developer = developer.await(),
                    background = background.await(),
                    updatedAt = updated_at,
                    url = url,
                    description = summary,
                    rating = GameRating(total_rating, total_rating_count)
                )
            )
        }
    }
}