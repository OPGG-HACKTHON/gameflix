package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSummary
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/games")
@RestController
class GameRestController(private val gameRepository: GameRepository) {

    @GetMapping
    fun getGames(pageable: Pageable) =
        gameRepository.getAllGames(pageable)
            .content
            .map { GameSummaryModel(it) }
            .let { MultipleGameSummaryModel(it) }

    data class GameSummaryModel(
        val name: String,
        val slug: String,
        val cover: String
    ) {
        constructor(gameSummary: GameSummary): this(
            gameSummary.slug.name,
            gameSummary.slug.slug,
            gameSummary.cover.toString()
        )
    }

    data class MultipleGameSummaryModel(val games: List<GameSummaryModel>)
}