package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.user.User
import org.hamcrest.Matchers.closeTo
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.ResultMatcher.matchAll
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

fun userModelContentsWith(user: User): ResultMatcher =
        matchAll(
            jsonPath("id", equalTo(user.id)),
            jsonPath("email", equalTo(user.email)),
            jsonPath("games", hasSize<String>(user.games.size)),
            multipleGameSummaryModelWith(path = "games", gameSummaries = user.games)
        )

fun multipleGameSummaryModelWith(gameSummaries: Collection<GameSummary>, path: String = "$.games"): ResultMatcher =
    gameSummaries.mapIndexed {index, summary -> gameSummaryModelWith(path = "$path.[${index}]", summary = summary)}
        .let { matchAll(*it.toTypedArray()) }

fun gameSummaryModelWith(summary: GameSummary, path: String = "$"): ResultMatcher =
    matchAll(
        jsonPath("$path.release_at", equalTo(summary.releaseAt)),
        jsonPath("$path.name", equalTo(summary.slug.name)),
        jsonPath("$path.slug", equalTo(summary.slug.slug)),
        jsonPath("$path.cover", equalTo(summary.cover)),
        jsonPath("$path.developer", equalTo(summary.developer))
    )

fun gameModelWith(game: Game): ResultMatcher =
    matchAll(
        gameSummaryModelWith(game.summary),
        jsonPath("updated_at", equalTo(game.detail.updatedAt)),
        jsonPath("description", equalTo(game.detail.description)),
        jsonPath("url", equalTo(game.detail.url)),
        jsonPath("genres", hasSize<String>(game.detail.genres.size)),
        jsonPath("platforms", hasSize<String>(game.detail.platforms.size)),
        jsonPath("rating_external", closeTo(game.detail.rating.rating.toDouble(), 0.01)),
        jsonPath("rating_external_count", equalTo(game.detail.rating.count))
    )