package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class IGDBGameRepository(private val igdbClient: IGDBClient) : GameRepository {

    override fun getAllGames(pageable: Pageable) =
        igdbClient.queryGetGames(pageable).toGameSummaries()

    override fun getAllGamesByIds(ids: Collection<Long>): Collection<Game> {
        TODO("Not yet implemented")
    }

    override fun findAllGamesByName(name: String, pageable: Pageable) {
        TODO("Not yet implemented")
    }

    private fun Page<IGDBGameSummary>.toGameSummaries() =
        let { igdbSummaries -> val idToCoverImage = igdbSummaries.content.map { it.cover }
            .toCollection(HashSet())
            .let { coverIds -> igdbClient.queryGetCoverImages(coverIds) }

            igdbSummaries.map { it.toGameSummary(idToCoverImage) }
        }

    private fun IGDBGameSummary.toGameSummary(idsToCover: Map<Int, IGDBCoverImage>) =
        GameSummary(GameSlug(name),
            idsToCover.getOrDefault(cover, IGDBCoverImage.NO_COVER_IMAGE).toURI())
}