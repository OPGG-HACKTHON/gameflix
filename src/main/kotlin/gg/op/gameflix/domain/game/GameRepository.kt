package gg.op.gameflix.domain.game

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GameRepository {
    fun getAllGames(pageable: Pageable): Page<Game>
    fun getAllGamesByIds(ids: Collection<Long>): Collection<Game>
    fun findAllGamesByName(name: String, pageable: Pageable)
}