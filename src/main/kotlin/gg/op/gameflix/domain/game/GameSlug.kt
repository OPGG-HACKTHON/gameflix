package gg.op.gameflix.domain.game

data class GameSlug(val name: String) {
    val slug: String = name.toSlug()
}

internal fun String.toSlug() = lowercase()
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .joinToString("-")
    .replace("-+".toRegex(), "-")