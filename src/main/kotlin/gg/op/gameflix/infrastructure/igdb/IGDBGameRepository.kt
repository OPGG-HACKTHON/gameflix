package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameDetail
import gg.op.gameflix.domain.game.GameRating
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URI

class IGDBGameRepository(private val igdbClient: IGDBClient) : GameRepository {

    override fun getAllGames(pageable: Pageable): Page<GameSummary> =
        igdbClient.queryGetGames(pageable)
            .toGameSummaries()

    override fun findGameBySlug(slug: GameSlug): Game? =
        igdbClient.queryGetGameBySlug(slug)
            ?.toGame()

    override fun findGamesByName(name: String, pageable: Pageable): Page<GameSummary> =
        igdbClient.queryGetGamesByName(name, pageable)
            .toGameSummaries()

    override fun getAllGamesByIds(ids: Collection<Long>): Collection<Game> {
        TODO("Not yet implemented")
    }

    private fun Page<IGDBGame>.toGameSummaries(): Page<GameSummary> {
        if (isEmpty) {
            return Page.empty()
        }
        return let { igdbGames -> igdbGames.content.map { it.cover }
            .toCollection(HashSet())
            .let { coverIds -> igdbClient.queryGetCoverImages(coverIds) }
            .associate { igdbCoverImage -> igdbCoverImage.id to igdbCoverImage.toURI() }
            .let { coverIdToURI -> igdbGames.map { it.toGameSummary(coverIdToURI) } }
        }
    }

    private fun IGDBGame.toGameSummary(coverIdToURI: Map<Int, URI>) =
        GameSummary(GameSlug(name), coverIdToURI.getOrDefault(cover, IGDBCoverImage.NO_COVER_IMAGE.toURI()))

    private fun IGDBGame.toGame() = Game(toGameSummary(), toGameDetail())

    private fun IGDBGame.toGameSummary() =
        GameSummary(GameSlug(name),
            igdbClient.queryGetCoverImages(listOf(cover)).first().toURI())

    private fun IGDBGame.toGameDetail() =
        GameDetail(releaseAt = first_release_date, updatedAt = updated_at,
            url = url,
            description = summary,
            genres = igdbClient.queryGetGenres(genres).map { it.toGenre() }.toHashSet(),
            platforms = igdbClient.queryGetPlatforms(platforms).map { it.toPlatform() }.toHashSet(),
            rating = GameRating(total_rating, total_rating_count))
}