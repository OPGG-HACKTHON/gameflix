package gg.op.gameflix.domain.user

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.GameSummaryService
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserGameService(
    private val gameSummaryService: GameSummaryService
) {
    @Transactional
    fun addGameToUser(user: User, slug: GameSlug): GameSummary {
        val gameSummaryFound = user.findGameBySlug(slug)
        if (gameSummaryFound != null) {
            return gameSummaryFound
        }
        return gameSummaryService.findGameSummaryBySlug(slug)
            ?.also { user.addGame(it) }
            ?: throw NoSuchElementException("Given game ${slug.name} not found")
    }
}