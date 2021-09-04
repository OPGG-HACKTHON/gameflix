package gg.op.gameflix.domain.game

import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class GameSummaryService(
    private val gameRepository: GameRepository,
    private val gameSummaryRepository: GameSummaryRepository
) {

    @Transactional
    fun findGameSummaryBySlug(slug: GameSlug): GameSummary? {
        val gameSummary = gameSummaryRepository.findFirstBySlug(slug)
        if (gameSummary != null) {
            return gameSummary
        }
        return gameRepository.findGameBySlug(slug)
            ?.summary
            ?.let { gameSummaryRepository.save(it) }
    }
}