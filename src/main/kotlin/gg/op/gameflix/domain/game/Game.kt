package gg.op.gameflix.domain.game

import java.net.URI

data class GameSummary(
    val slug: GameSlug,
    val cover: URI
)

data class Game(val slug: GameSlug,
    val detail: GameDetail,
    val genres: Collection<Genre>,
    val platforms: Collection<Platform>,
    val rating: GameRating
)

data class GameDetail(val releaseAt: Int, val updatedAt: Int, val cover: String, val url: String)

enum class Genre

enum class Platform

data class GameRating(val rating: Float, val count: Int)