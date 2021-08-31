package gg.op.gameflix.domain.game

import java.net.URI

data class Game(
    val summary: GameSummary,
    val detail: GameDetail
)

data class GameSummary(
    val slug: GameSlug,
    val cover: URI
)

data class GameDetail(
    val releaseAt: Int,
    val updatedAt: Int,
    val url: String,
    val description: String,
    val genres: Set<Genre>,
    val platforms: Set<Platform>,
    val rating: GameRating
)

data class GameSlug(val name: String, val slug: String) {
    constructor(name: String) : this(name, name.toSlug())
}

private fun String.toSlug() = lowercase()
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .joinToString("-")
    .replace("-+".toRegex(), "-")

data class Genre(val name: String)

data class Platform(val name: String)

data class GameRating(val rating: Float, val count: Int)
