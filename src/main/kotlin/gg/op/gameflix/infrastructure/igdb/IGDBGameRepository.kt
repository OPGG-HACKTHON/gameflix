package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameDetail
import gg.op.gameflix.domain.game.GameRating
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

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

    private fun Collection<IGDBGame>.toGameSummaries(): List<GameSummary> {
        if (isEmpty())
            return emptyList()
        return let { igdbGames ->
            igdbGames.map { it.cover }
                .let { coverIds -> runBlocking { igdbClient.queryGetCoverImages(coverIds) } }
                .associate { igdbCoverImage -> igdbCoverImage.id to igdbCoverImage.toCoverURI() }
                .let { coverIdToURI -> igdbGames.map { it.toGameSummary(coverIdToURI) } }
        }
    }

    private fun Page<IGDBGame>.toGameSummaries(): Page<GameSummary> {
        if (isEmpty) {
            return Page.empty()
        }
        return let { igdbGames -> igdbGames.content.map { it.cover }
            .let { coverIds -> runBlocking { igdbClient.queryGetCoverImages(coverIds) } }
            .associate { igdbCoverImage -> igdbCoverImage.id to igdbCoverImage.toCoverURI() }
            .let { coverIdToURI -> igdbGames.map { it.toGameSummary(coverIdToURI) } }
        }
    }

    private fun IGDBGame.toGameSummary(coverIdToURI: Map<Int, String>) =
        GameSummary(GameSlug(name),
            coverIdToURI.getOrDefault(cover, IGDBImage.NO_COVER_IMAGE.toCoverURI()),
            first_release_date)

    private fun IGDBGame.toGame(): Game = runBlocking {
        val image = async { igdbClient.queryGetCoverImages(listOf(cover)).firstOrNull()?.toCoverURI() ?: IGDBImage.NO_COVER_IMAGE.toCoverURI()}
        val genres = async { igdbClient.queryGetGenres(genres).map { it.toGenre() }.toHashSet() }
        val platforms = async { igdbClient.queryGetPlatforms(platforms).map { it.toPlatform() }.toHashSet() }
        val developer = async { igdbClient.queryGetDeveloperByInvolvedCompanies(involved_companies)?.slug ?: "NOT FOUND" }
        val background = async { igdbClient.queryGetScreenShots(screenshots).firstOrNull()?.toBackgroundURI() ?: IGDBImage.NO_COVER_IMAGE.toBackgroundURI() }

        runBlocking {
            Game(
                GameSummary(GameSlug(name), image.await(), first_release_date),
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