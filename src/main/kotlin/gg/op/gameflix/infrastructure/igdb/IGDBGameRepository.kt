package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class IGDBGameRepository : GameRepository {
    override fun getAllGames(pageable: Pageable): Page<Game> {
        TODO("Not yet implemented")
    }

    override fun getAllGamesByIds(ids: Collection<Long>): Collection<Game> {
        TODO("Not yet implemented")
    }

    override fun findAllGamesByName(name: String, pageable: Pageable) {
        TODO("Not yet implemented")
    }
}