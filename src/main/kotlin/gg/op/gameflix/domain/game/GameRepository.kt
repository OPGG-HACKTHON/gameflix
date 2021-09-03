package gg.op.gameflix.domain.game

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GameRepository {
    fun getAllGames(pageable: Pageable): Page<GameSummary>
    fun findGameBySlug(slug: GameSlug): Game?
    fun findGamesByName(name: String, pageable: Pageable): Page<GameSummary>
    fun getAllGamesByIds(ids: Collection<Long>): Collection<Game>
}

interface GameSummaryRepository: JpaRepository<GameSummary, Long> {
    fun findFirstBySlug(slug: GameSlug): GameSummary?
}