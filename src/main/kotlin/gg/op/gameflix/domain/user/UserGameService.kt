package gg.op.gameflix.domain.user

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.GameSummaryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserGameService(
    private val summaryService: GameSummaryService,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun addGameToUser(user: User, slug: GameSlug): GameSummary {
        val gameSummaryFound = user.findGameBySlug(slug)
        if (gameSummaryFound != null) {
            return gameSummaryFound
        }
        return summaryService.findGameSummaryBySlug(slug)
            ?.also { user.addGame(it)
                userRepository.save(user) }
            ?: throw NoSuchElementException("Given game ${slug.name} not found")
    }

    @Transactional(readOnly = true)
    fun findGameInUser(user: User, slug: GameSlug): Game? =
        user.games.find { gameSummary -> gameSummary.slug == slug }
            ?.let { gameSummary -> gameRepository.findFirstGameBySlug(gameSummary.slug) }

    @Transactional
    fun deleteGameInUser(user: User, slug: GameSlug) {
        val isGameRemoved = user.games.removeIf { gameSummary -> gameSummary.slug == slug}
        if (!isGameRemoved) {
            throw NoSuchElementException("Given game ${slug.name} not found")
        }
        userRepository.save(user)
    }
}