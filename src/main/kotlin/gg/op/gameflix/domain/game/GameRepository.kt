package gg.op.gameflix.domain.game

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GameRepository {
    fun findAllGameSummaries(pageable: Pageable): Page<GameSummary>
    fun findAllGameSummariesByName(name: String, pageable: Pageable): Page<GameSummary>
    fun findAllGameSummariesBySlugs(slugs: Collection<GameSlug>): Collection<GameSummary>
    fun findFirstGameBySlug(slug: GameSlug): Game?
}

interface GameSummaryRepository: JpaRepository<GameSummary, Long> {
    fun findFirstBySlug(slug: GameSlug): GameSummary?
}