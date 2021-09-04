package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserGameService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/users")
@RestController
class UserRestController(
    private val userGameService: UserGameService
) {
    @ResponseStatus(CREATED)
    @PostMapping
    fun postUsers(@AuthenticationPrincipal user: User): UserModel =
        UserModel(user)

    @PreAuthorize("#id == #user.id")
    @ResponseStatus(CREATED)
    @PostMapping("/{id}/games")
    fun postUserGames(@PathVariable id: String, @AuthenticationPrincipal user: User,
        @RequestBody requestDTO: GamePostRequestDTO): GameSummaryModel =
        userGameService.addGameToUser(user, GameSlug(requestDTO.slug))
            .let { GameSummaryModel(it) }
}

data class UserModel(
    val id: String,
    val email: String,
    val games: List<GameSummaryModel>
) {
    constructor(user: User): this(
        user.id,
        user.email,
        user.games.map { GameSummaryModel(it) }
    )
}

data class GamePostRequestDTO(
    val slug: String
)