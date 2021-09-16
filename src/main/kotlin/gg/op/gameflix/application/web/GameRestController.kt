package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.toSlug
import gg.op.gameflix.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/games")
@RestController
class GameRestController(private val gameRepository: GameRepository) {

    @GetMapping
    fun getGames(pageable: Pageable, @AuthenticationPrincipal user: User?): PagedGameSummaryModel =
        gameRepository.findAllGameSummaries(pageable)
            .map { gameSummary -> GameSummaryModel(gameSummary, user?.isCollectedSummary(gameSummary) ?: false) }
            .let { PagedGameSummaryModel(it) }

    @GetMapping(params = ["search"])
    fun getGamesByName(search: String, pageable: Pageable, @AuthenticationPrincipal user: User?): PagedGameSummaryModel =
        gameRepository.findAllGameSummariesByName(search, pageable)
            .map { gameSummary -> GameSummaryModel(gameSummary, user?.isCollectedSummary(gameSummary) ?: false) }
            .let { PagedGameSummaryModel(it)}

    @GetMapping("/{slug}")
    fun getGameBySlug(@PathVariable slug: String, @AuthenticationPrincipal user: User?): ResponseEntity<GameModel> =
        gameRepository.findFirstGameBySlug(GameSlug(slug))
            ?.let { GameModel(it, user?.isCollectedSummary(it.summary) ?: false) }
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
    constructor(gamesPage: Page<GameSummaryModel>): this(
        games = gamesPage.content,
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
}

@Suppress("kotlin:S117")
data class GameModel(
    val name: String,
    val slug: String,
    val cover: String,
    val release_at: Int,
    val developer: String,
    val updated_at: Int,
    val description: String,
    val url: String,
    val genres: List<String>,
    val platforms: List<String>,
    val rating_external: Float,
    val rating_external_count: Int,
    val background: String,
    val collected: Boolean = false
    ) {
    constructor(game: Game, collected: Boolean = false): this(
        name = game.summary.slug.name,
        slug = game.summary.slug.slug,
        cover = game.summary.cover,
        release_at = game.summary.releaseAt,
        developer = game.summary.developer,
        description = game.detail.description,
        updated_at = game.detail.updatedAt,
        url = game.detail.url,
        genres = game.detail.genres.map { it.name },
        platforms = game.detail.platforms.map { it.name },
        rating_external = game.detail.rating.rating,
        rating_external_count = game.detail.rating.count,
        background = game.detail.background,
        collected = collected
    )
}

@Suppress("kotlin:S117")
data class GameSummaryModel(
    val name: String,
    val slug: String,
    val cover: String,
    val release_at: Int,
    val store: String,
    val developer: String,
    val collected: Boolean = false,
) {
    constructor(gameSummary: GameSummary, collected: Boolean = false): this(
        name = gameSummary.slug.name,
        slug = gameSummary.slug.slug,
        cover = gameSummary.cover,
        release_at = gameSummary.releaseAt,
        store = gameSummary.store.name.toSlug(),
        developer = gameSummary.developer,
        collected = collected
    )
}
