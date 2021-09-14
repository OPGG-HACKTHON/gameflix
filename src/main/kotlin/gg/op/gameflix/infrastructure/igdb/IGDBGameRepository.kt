package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameDetail
import gg.op.gameflix.domain.game.GameRating
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
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
        return let { igdbGames -> igdbGames.map { it.cover }
            .toCollection(HashSet())
            .let { coverIds -> igdbClient.queryGetCoverImages(coverIds) }
            .associate { igdbCoverImage -> igdbCoverImage.id to igdbCoverImage.toCoverURI() }
            .let { coverIdToURI -> igdbGames.map { it.toGameSummary(coverIdToURI) } }
        }
    }

    private fun Page<IGDBGame>.toGameSummaries(): Page<GameSummary> {
        if (isEmpty) {
            return Page.empty()
        }
        return let { igdbGames -> igdbGames.content.map { it.cover }
            .toCollection(HashSet())
            .let { coverIds -> igdbClient.queryGetCoverImages(coverIds) }
            .associate { igdbCoverImage -> igdbCoverImage.id to igdbCoverImage.toCoverURI() }
            .let { coverIdToURI -> igdbGames.map { it.toGameSummary(coverIdToURI) } }
        }
    }

    private fun IGDBGame.toGameSummary(coverIdToURI: Map<Int, String>) =
        GameSummary(GameSlug(name),
            coverIdToURI.getOrDefault(cover, IGDBImage.NO_COVER_IMAGE.toCoverURI()),
            first_release_date)

    private fun IGDBGame.toGame() = Game(toGameSummary(), toGameDetail())

    private fun IGDBGame.toGameSummary() =
        GameSummary(GameSlug(name),
            igdbClient.queryGetCoverImages(listOf(cover)).first().toCoverURI(),
            first_release_date)

    private fun IGDBGame.toGameDetail() =
        GameDetail(
            updatedAt = updated_at, url = url,
            description = summary,
            genres = igdbClient.queryGetGenres(genres).map { it.toGenre() }.toHashSet(),
            platforms = igdbClient.queryGetPlatforms(platforms).map { it.toPlatform() }.toHashSet(),
            rating = GameRating(total_rating, total_rating_count),
            developer = igdbClient.queryGetDeveloperByInvolvedCompanies(involved_companies)?.slug ?: "NOT FOUND",
            background = igdbClient.queryGetScreenShots(screenshots).firstOrNull()?.toBackgroundURI() ?: IGDBImage.NO_COVER_IMAGE.toBackgroundURI()
        )
}