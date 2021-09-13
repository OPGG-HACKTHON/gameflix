package gg.op.gameflix.domain.game

import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class GameSummaryService(
    private val gameRepository: GameRepository,
    private val gameSummaryRepository: GameSummaryRepository
) {

    @Transactional
    fun findGameSummariesBySlugsAndStore(slugs: Collection<GameSlug>, store: Store): Collection<GameSummary> {
        val slugsToNullableSummary = slugs.asSequence()
                .associateWith { gameSummaryRepository.findFirstBySlugAndStore(it, store) }
        val summariesFound = slugsToNullableSummary.findAndSaveAllNullableSummariesWithStore(store)
        return slugsToNullableSummary
            .mapNotNull { (slug, summary) -> summary ?: (summariesFound.find { slug == it.slug }) }
    }

    @Transactional
    fun findGameSummaryBySlug(slug: GameSlug): GameSummary? {
        val gameSummary = gameSummaryRepository.findFirstBySlugAndStore(slug, null)
        if (gameSummary != null) {
            return gameSummary
        }
        return gameRepository.findFirstGameBySlug(slug)
            ?.summary
            ?.let { gameSummaryRepository.save(it) }
    }

    private fun Map<GameSlug, GameSummary?>.findAndSaveAllNullableSummariesWithStore(store: Store): Collection<GameSummary> {
        val slugsNotFound = filterValues { it == null }.keys
        if (slugsNotFound.isEmpty()) {
            return emptyList()
        }
        return slugsNotFound
            .let(gameRepository::findAllGameSummariesBySlugs)
            .map {
                it.store = store
                gameSummaryRepository.save(it)
            }
            .toList()
    }

}