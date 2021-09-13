package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.toSlug
import org.springframework.beans.support.PagedListHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/games")
@RestController
class GameRestController(private val gameRepository: GameRepository) {

    @GetMapping
    fun getGames(pageable: Pageable): PagedGameSummaryModel =
        gameRepository.findAllGameSummaries(pageable)
            .let { PagedGameSummaryModel(it) }

    @GetMapping(params = ["search"])
    fun getGamesByName(search: String, pageable: Pageable): PagedGameSummaryModel =
        gameRepository.findAllGameSummariesByName(search, pageable)
            .let { PagedGameSummaryModel(it) }

    @GetMapping("/{slug}")
    fun getGameBySlug(@PathVariable slug: String): ResponseEntity<GameModel> =
        gameRepository.findFirstGameBySlug(GameSlug(slug))
            ?.let { GameModel(it) }
            ?.let { gameModel -> ResponseEntity.ok(gameModel) } ?: ResponseEntity.notFound().build()
}

data class PagedGameSummaryModel(
    val games: List<GameSummaryModel>,
    val number: Int,
    val size: Int,
    val numberOfElements: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val totalPages: Int,
    val totalElements: Long
) {
    constructor(gamesPage: Page<GameSummary>): this(
        games = gamesPage.map { GameSummaryModel(it) }.content,
        number = gamesPage.number,
        size = gamesPage.size,
        numberOfElements = gamesPage.numberOfElements,
        isFirst = gamesPage.isFirst,
        isLast = gamesPage.isLast,
        hasNext = gamesPage.hasNext(),
        hasPrevious = gamesPage.hasPrevious(),
        totalPages = gamesPage.totalPages,
        totalElements = gamesPage.totalElements
    )
    constructor(gamesPage: PagedListHolder<GameSummary>): this(
        games = gamesPage.source.map { GameSummaryModel(it) },
        number = gamesPage.page,
        size = gamesPage.pageSize,
        numberOfElements = gamesPage.nrOfElements,
        isFirst = gamesPage.isFirstPage,
        isLast = gamesPage.isLastPage,
        hasNext = !gamesPage.isLastPage,
        hasPrevious = !gamesPage.isFirstPage,
        totalPages = gamesPage.pageCount,
        totalElements = gamesPage.nrOfElements.toLong()
    )
}

@Suppress("kotlin:S117")
data class GameModel(
    val name: String,
    val slug: String,
    val release_at: Int,
    val updated_at: Int,
    val cover: String,
    val description: String,
    val url: String,
    val genres: List<String>,
    val platforms: List<String>,
    val rating_external: Float,
    val rating_external_count: Int,
    val developer: String,
    val background: String
    ) {
    constructor(game: Game): this(
        name = game.summary.slug.name,
        slug = game.summary.slug.slug,
        cover = game.summary.cover,
        description = game.detail.description,
        release_at = game.detail.releaseAt,
        updated_at = game.detail.updatedAt,
        url = game.detail.url,
        genres = game.detail.genres.map { it.name },
        platforms = game.detail.platforms.map { it.name },
        rating_external = game.detail.rating.rating,
        rating_external_count = game.detail.rating.count,
        developer = game.detail.developer,
        background = game.detail.background
    )
}

data class GameSummaryModel(
    val name: String,
    val slug: String,
    val cover: String,
    val store: String
) {
    constructor(gameSummary: GameSummary): this(
        gameSummary.slug.name,
        gameSummary.slug.slug,
        gameSummary.cover,
        gameSummary.store?.name?.toSlug() ?: ""
    )
}
